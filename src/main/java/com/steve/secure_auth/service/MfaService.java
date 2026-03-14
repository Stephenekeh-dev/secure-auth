package com.steve.secure_auth.service;

import com.steve.secure_auth.model.MfaSecret;
import com.steve.secure_auth.model.User;
import com.steve.secure_auth.repository.MfaSecretRepository;
import com.steve.secure_auth.repository.UserRepository;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
public class MfaService {

    private final MfaSecretRepository mfaSecretRepository;
    private final UserRepository userRepository;

    public MfaService(MfaSecretRepository mfaSecretRepository,
                      UserRepository userRepository) {
        this.mfaSecretRepository = mfaSecretRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MfaSetupResult setup(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Delete existing MFA secret if present
        mfaSecretRepository.findByUser(user)
                .ifPresent(mfaSecretRepository::delete);

        // Generate new secret
        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        String secret = secretGenerator.generate();

        // Save to DB (not yet enabled)
        MfaSecret mfaSecret = new MfaSecret();
        mfaSecret.setUser(user);
        mfaSecret.setSecret(secret);
        mfaSecret.setEnabled(false);
        mfaSecretRepository.save(mfaSecret);

        // Generate QR code URL
        QrData qrData = new QrData.Builder()
                .label(user.getEmail())
                .secret(secret)
                .issuer("SecureAuth")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        String qrCodeUrl = generateQrCodeUrl(qrData);

        return new MfaSetupResult(secret, qrCodeUrl);
    }

    @Transactional
    public void verify(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        MfaSecret mfaSecret = mfaSecretRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("MFA not set up for this user"));

        if (!isCodeValid(mfaSecret.getSecret(), code)) {
            throw new IllegalArgumentException("Invalid MFA code");
        }

        mfaSecret.setEnabled(true);
        mfaSecretRepository.save(mfaSecret);
    }

    public boolean isCodeValid(String secret, String code) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        verifier.setAllowedTimePeriodDiscrepancy(2);
        return verifier.isValidCode(secret, code);
    }

    public boolean isMfaEnabled(User user) {
        return mfaSecretRepository.findByUser(user)
                .map(MfaSecret::isEnabled)
                .orElse(false);
    }

    private String generateQrCodeUrl(QrData qrData) {
        try {
            QrGenerator generator = new ZxingPngQrGenerator();
            byte[] imageData = generator.generate(qrData);
            return getDataUriForImage(imageData, generator.getImageMimeType());
        } catch (QrGenerationException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    // Inner result class
    public record MfaSetupResult(String secret, String qrCodeUrl) {

    }
}