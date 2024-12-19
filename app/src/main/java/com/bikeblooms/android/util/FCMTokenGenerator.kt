package com.bikeblooms.android.util

import com.bikeblooms.android.util.FirebaseConstants.FCM.FIREBASE_MESSAGING_SCOPE
import com.google.auth.oauth2.GoogleCredentials
import com.google.common.collect.Lists
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

class FCMTokenGenerator {
    fun getAccessToken(): String {
        try {
            val adminSdkZhy =
                "{\n" + "  \"type\": \"service_account\",\n" + "  \"project_id\": \"bike-blooms\",\n" + "  \"private_key_id\": \"7c9b60ab1dd1993f1b72947c1c9d4113c5daed18\",\n" + "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCapbG2aydJ1rj1\\nfyd4ZYoU6l9NhQR7GYoI3COtLZdJV9CFmEquKzdQrMFWbPEYajwR8LJWbSxGEnjG\\nL6LjwQGa9s9TAGwYx2Nzwsl45bKzS/Fl3/y9CE272FnDvSlw4o76oSzVh91oI0hd\\n5TFP2zcUSu/PLRVfKeSM/cRmk9+W1hKKyIvM4+5jZMVf5AtGE/v72gsKWnNzsMZC\\nPXtIVVKa6FP74ZfjkRg8+ShnHjFIqfe/ePV+V1v4eTTUEb1PylrQbmSN6TSRCX59\\nC9ujb70Xx4FyMhw8DWN9m8iI0gBBXAn4vQxIx0DbBwr30Vrn0noxw5UD8Yhae3Jv\\nDqAXneJjAgMBAAECggEAJca974qjUUUzbzy9XoqnPU63Mw+ANbXaYymrSV1urErY\\nNBose/kwjPN2Uu9PwaAJxnQejEAk5YUTnY37Y1k9VCZgJK1gunjSUW/OmUq66VNu\\nx2z8pwyFOJKz8r2dhubA+HYwpoSG0C52OgGHlzM8dCeOBvTDWLi6DLrcpBYRMx38\\nqQGoGHLFgzHOOnc5RLegHsY7EeQm1hFMU8aWy8VPP6zjvWzmyC77MbyDZQLmwioQ\\nIxPzqtpvUGcrjU9AKPdGkQT1GNMWrXpUaAFBrNBvay98ONS7NqXGWUJ7Lkssh/Uv\\ndv7KfN3sSBaLw7svRbuJx7xyncNug5uTWyOpdH2dMQKBgQDOfHgJ2w6dQ4N+mFqf\\nmGHcAHlZVVLNMqU5YO7Gze5CVCp9jrYGcRgoyf9TEV1e1PiomZSQw4jw1aNRTmYx\\ncaZpIWRW3I9OzY+hs5NcbV6aqBFPTN6D3pBpCFUeqAn4/A66oorlKLig5SmIfqZw\\n1RLUm4UV3X+ZrAff1zOjAtYb+wKBgQC/uv8MO7KmJjUUHZfZSCN83gdSq7AF6KUK\\nqgci16DjAYvX1CBCWrKTrmc1sDm/7uwSQIQJJv/0uavA0GIbr/eoDnMAJuHQInp8\\n6mK0z9nFGlpu17T6zLzg41AXWyekNE7eygPBVPsU2r9OFRSrucL/KgNrlv0tdiVM\\nF+k+OeDeuQKBgFy4rdqXII/kAkc1uPQTpFX7bIJ9+wmj5WfHrFFfpc7pJo9tJzXr\\nURL36gxuLsRn1CU5meoLtBGZjZX0I5WuWDC7SuhqcHvNeOL+1XxarHG8aNF4Ti9f\\nIO/ZSwfSI3BIF4Edfkt7JrUxUp41aR4fwC3yjCeOaD8kbgDJDSn1GxpDAoGAHar0\\n3lK6gSU2X2d44MFd5GuC3JL+iNuH9k97DTQqjBXoKryDRWE9csaw58jeFsoEs4pS\\nJAxp3NeCMTLNi5U+ED40I1jg5lD/fSbToUDsZKiAR9iPA6P8shvaf9K2Hp2NesHt\\ntNPOilqS8aUnbq8u8kbxAov5nzhB6zVj/SH9ntkCgYEAl7L/ugGfJlDHOq1pmddK\\n8rM+HUL1DPS2C6tdmL3HQF32RKLHv/ud+t8eY7nRDJcf7BOHybI3puTmJ3mTdDXt\\n7OCF8sWi9uVGuDBm4JkonUuU9oH3ScgKOCTaWkUbWMonjE97pKoCTrqhqkvO6OAG\\ndTyOSFKpQlmns6vfvfS42Lw=\\n-----END PRIVATE KEY-----\\n\",\n" + "  \"client_email\": \"firebase-adminsdk-zhy67@bike-blooms.iam.gserviceaccount.com\",\n" + "  \"client_id\": \"107438793331761644281\",\n" + "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" + "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" + "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" + "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-zhy67%40bike-blooms.iam.gserviceaccount.com\",\n" + "  \"universe_domain\": \"googleapis.com\"\n" + "}\n"

            val stream = ByteArrayInputStream(adminSdkZhy.toByteArray(StandardCharsets.UTF_8))

            val googleCredentials = GoogleCredentials.fromStream(stream)
                .createScoped(Lists.newArrayList(FIREBASE_MESSAGING_SCOPE))
            googleCredentials.refresh()
            return googleCredentials.accessToken.tokenValue
        } catch (e: IOException) {
            return ""
        }
        return ""
    }
}