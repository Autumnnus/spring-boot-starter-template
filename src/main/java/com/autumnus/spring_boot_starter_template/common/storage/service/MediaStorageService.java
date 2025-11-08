package com.autumnus.spring_boot_starter_template.common.storage.service;

import com.autumnus.spring_boot_starter_template.common.storage.config.S3Properties;
import com.autumnus.spring_boot_starter_template.common.storage.dto.MediaAsset;
import com.autumnus.spring_boot_starter_template.common.storage.exception.MediaStorageException;
import com.autumnus.spring_boot_starter_template.common.storage.exception.MediaValidationException;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaFileDescriptor;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaKind;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaManifest;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaUpload;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaVariant;
import com.autumnus.spring_boot_starter_template.common.storage.model.MediaVariantDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class MediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(MediaStorageService.class);

    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");

    private static final List<MediaVariantDefinition> IMAGE_VARIANTS = List.of(
            new MediaVariantDefinition(MediaVariant.WEB, 1920, 1080, 0.85f),
            new MediaVariantDefinition(MediaVariant.MOBILE, 1080, 1080, 0.8f),
            new MediaVariantDefinition(MediaVariant.THUMB, 320, 320, 0.8f)
    );

    private final S3Client s3Client;
    private final S3Properties properties;
    private final ObjectMapper objectMapper;

    public MediaStorageService(S3Client s3Client, S3Properties properties, ObjectMapper objectMapper) {
        this.s3Client = s3Client;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public List<MediaAsset> storeAll(MediaKind kind, String purpose, List<MediaUpload> uploads) {
        Objects.requireNonNull(kind, "Media kind is required");
        if (uploads == null || uploads.isEmpty()) {
            throw new MediaValidationException("At least one file must be provided");
        }
        if (kind == MediaKind.IMAGE && (uploads.size() < 1 || uploads.size() > 100)) {
            throw new MediaValidationException("Image uploads must contain between 1 and 100 files");
        }
        final List<MediaAsset> results = new ArrayList<>(uploads.size());
        for (MediaUpload upload : uploads) {
            results.add(store(kind, purpose, upload));
        }
        return results;
    }

    public MediaAsset store(MediaKind kind, String purpose, MediaUpload upload) {
        Objects.requireNonNull(kind, "Media kind is required");
        Objects.requireNonNull(upload, "Upload payload is required");
        validatePurpose(purpose);
        final byte[] content = upload.content();
        if (content == null || content.length == 0) {
            throw new MediaValidationException("File content cannot be empty");
        }
        final String mimeType = normalizeMime(upload.contentType());
        kind.validate(mimeType, upload.size());
        final String extension = kind.resolveExtension(upload.originalFilename(), mimeType);

        final LocalDate now = LocalDate.now();
        final String basePath = buildBasePath(kind, purpose, content, now);
        final String originalKey = basePath + "original." + extension;

        putObject(originalKey, mimeType, content);
        final MediaFileDescriptor originalDescriptor = new MediaFileDescriptor(
                originalKey,
                buildPublicUrl(originalKey),
                mimeType,
                content.length
        );

        final Map<MediaVariant, MediaFileDescriptor> variants = new LinkedHashMap<>();
        if (kind == MediaKind.IMAGE) {
            variants.putAll(processImageVariants(basePath, extension, mimeType, content));
        }

        final MediaManifest manifest = new MediaManifest(originalDescriptor, variants);
        final String manifestKey = basePath + "manifest.json";
        writeManifest(manifestKey, manifest);

        return new MediaAsset(basePath, manifestKey, manifest);
    }

    public MediaAsset replace(MediaManifest existingManifest, MediaKind kind, String purpose, MediaUpload upload) {
        final MediaAsset asset = store(kind, purpose, upload);
        if (existingManifest != null) {
            delete(existingManifest);
        }
        return asset;
    }

    public void delete(MediaManifest manifest) {
        if (manifest == null) {
            return;
        }
        String basePath = null;
        if (manifest.original() != null) {
            deleteKey(manifest.original().key());
            final String originalKey = manifest.original().key();
            final int lastSlash = originalKey.lastIndexOf('/');
            if (lastSlash > -1) {
                basePath = originalKey.substring(0, lastSlash + 1);
            }
        }
        if (manifest.variants() != null) {
            manifest.variants().values().forEach(descriptor -> deleteKey(descriptor.key()));
        }
        if (basePath != null) {
            deleteKey(basePath + "manifest.json");
        }
    }

    private Map<MediaVariant, MediaFileDescriptor> processImageVariants(String basePath, String extension, String mimeType, byte[] content) {
        final Map<MediaVariant, MediaFileDescriptor> descriptors = new LinkedHashMap<>();
        final BufferedImage originalImage;
        try {
            originalImage = ImageIO.read(new ByteArrayInputStream(content));
            if (originalImage == null) {
                throw new MediaValidationException("Unable to read image content");
            }
        } catch (IOException e) {
            throw new MediaValidationException("Failed to read image content");
        }
        for (MediaVariantDefinition definition : IMAGE_VARIANTS) {
            final MediaVariant variant = definition.variant();
            final String key = basePath + "variants/" + variant.name().toLowerCase(Locale.ROOT) + "." + extension;
            final byte[] variantBytes = resizeImage(originalImage, definition, extension);
            putObject(key, mimeType, variantBytes);
            descriptors.put(variant, new MediaFileDescriptor(
                    key,
                    buildPublicUrl(key),
                    mimeType,
                    variantBytes.length
            ));
        }
        return descriptors;
    }

    private byte[] resizeImage(BufferedImage originalImage, MediaVariantDefinition definition, String extension) {
        final int targetWidth = definition.maxWidth();
        final int targetHeight = definition.maxHeight();
        final BufferedImage sourceImage = normalizeImageForExtension(originalImage, extension);
        if (sourceImage.getWidth() <= targetWidth && sourceImage.getHeight() <= targetHeight) {
            try {
                final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                ImageIO.write(sourceImage, extension, stream);
                return stream.toByteArray();
            } catch (IOException e) {
                throw new MediaStorageException("Failed to buffer original image", e);
            }
        }
        try {
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Thumbnails.of(sourceImage)
                    .size(targetWidth, targetHeight)
                    .outputFormat(extension)
                    .outputQuality(definition.quality())
                    .keepAspectRatio(true)
                    .toOutputStream(stream);
            return stream.toByteArray();
        } catch (IOException e) {
            throw new MediaStorageException("Failed to create image variant", e);
        }
    }

    private void writeManifest(String manifestKey, MediaManifest manifest) {
        try {
            final byte[] manifestBytes = objectMapper.writeValueAsBytes(manifest);
            putObject(manifestKey, "application/json", manifestBytes);
        } catch (JsonProcessingException e) {
            throw new MediaStorageException("Unable to serialize media manifest", e);
        }
    }

    private void putObject(String key, String contentType, byte[] content) {
        try {
            final PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) content.length)
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(content));
        } catch (S3Exception ex) {
            throw new MediaStorageException("Failed to upload object to S3", ex);
        }
    }

    private void deleteKey(String key) {
        try {
            final DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
        } catch (NoSuchKeyException ex) {
            log.warn("Attempted to delete missing S3 object: {}", key);
        } catch (S3Exception ex) {
            throw new MediaStorageException("Failed to delete S3 object: " + key, ex);
        }
    }

    private String buildBasePath(MediaKind kind, String purpose, byte[] content, LocalDate now) {
        final String sanitizedPurpose = sanitizePurpose(purpose);
        return "media/" + kind.name().toLowerCase(Locale.ROOT) + "/" + sanitizedPurpose + "/"
                + YEAR_FORMATTER.format(now) + "/" + MONTH_FORMATTER.format(now) + "/" + DAY_FORMATTER.format(now) + "/"
                + computeSha12(content) + "-" + UUID.randomUUID() + "/";
    }

    private String computeSha12(byte[] content) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(content);
            return HexFormat.of().formatHex(hash).substring(0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private String buildPublicUrl(String key) {
        if (StringUtils.hasText(properties.getPublicBaseUrl())) {
            return properties.getPublicBaseUrl().replaceAll("/$", "") + "/" + key;
        }
        return "https://" + properties.getBucket() + ".s3." + properties.getRegion() + ".amazonaws.com/" + key;
    }

    private void validatePurpose(String purpose) {
        if (!StringUtils.hasText(purpose)) {
            throw new MediaValidationException("Purpose is required");
        }
    }

    private String sanitizePurpose(String purpose) {
        final String normalized = purpose.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]", "-");
        final String cleaned = normalized.replaceAll("-+", "-").replaceAll("^-|-$", "");
        return cleaned.isBlank() ? "general" : cleaned;
    }

    private String normalizeMime(String mimeType) {
        return mimeType == null ? null : mimeType.toLowerCase(Locale.ROOT);
    }

    private BufferedImage normalizeImageForExtension(BufferedImage image, String extension) {
        if (!"jpg".equalsIgnoreCase(extension) && !"jpeg".equalsIgnoreCase(extension)) {
            return image;
        }
        final BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics = converted.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return converted;
    }
}
