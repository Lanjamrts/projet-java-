package com.marketplace.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Service dédié à la génération de QR codes en base64.
 * Extrait de ProductService (violation du principe de responsabilité unique).
 */
@Slf4j
@Service
public class QrCodeService {

    /**
     * Génère un QR code PNG encodé en base64 à partir d'une URL.
     * Retourne null si la génération échoue (erreur loggée).
     */
    public String generate(String url) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            log.error("Erreur génération QR code pour url={} : {}", url, e.getMessage());
            return null;
        }
    }
}