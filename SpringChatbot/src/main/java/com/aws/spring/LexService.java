package com.aws.spring;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lexruntime.LexRuntimeClient;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.lexruntime.model.LexRuntimeException;
import software.amazon.awssdk.services.lexruntime.model.PostTextRequest;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.ComprehendException;
import software.amazon.awssdk.services.comprehend.model.DetectDominantLanguageRequest;
import software.amazon.awssdk.services.comprehend.model.DetectDominantLanguageResponse;
import software.amazon.awssdk.services.comprehend.model.DominantLanguage;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;
import software.amazon.awssdk.services.translate.model.TranslateException;
import java.util.*;
import software.amazon.awssdk.services.lexruntime.model.PostTextResponse;

@Component
public class LexService {

    public String getText(String text) {

        Region region = Region.AP_NORTHEAST_2;
        LexRuntimeClient lexRuntimeClient = LexRuntimeClient.builder()
                .region(region)
                .build();

        String engMessage ="";
        try {

            // Need to determine the language.
            String lanCode = DetectLanguage(text);

            // If the lanCode is NOT Eng - then we need to translate the message to English to pass to Amazon Lex.
            if (lanCode.compareTo("kr")  !=0)
                engMessage = textTranslateToEn(lanCode, text);
            else
                engMessage=text;

            String userId =  "vusckddms2815@gmail.com";

            Map<String,String> sessionAttributes = new HashMap<>();
            PostTextRequest textRequest = PostTextRequest.builder()
                    .botName("BMOBank")
                    .botAlias("TestBotAlias")
                    .inputText(engMessage)
                    .userId(userId)
                    .sessionAttributes(sessionAttributes)
                    .build();

            PostTextResponse textResponse = lexRuntimeClient.postText(textRequest);
            String message = textResponse.message();

            // If not EN, we need to translate the text back
            String outputText ="";
            if (lanCode.compareTo("kr")  !=0)
                outputText = textTranslateFromEn(lanCode, message);
            else
                outputText = message;

            return outputText ;

        } catch (LexRuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";
    }

    private String DetectLanguage(String text) {

        Region region = Region.AP_NORTHEAST_2;
        ComprehendClient comClient = ComprehendClient.builder()
                .region(region)
                .build();

        try {

            String lanCode = "";
            DetectDominantLanguageRequest request = DetectDominantLanguageRequest.builder()
                    .text(text)
                    .build();

            DetectDominantLanguageResponse resp = comClient.detectDominantLanguage(request);
            List<DominantLanguage> allLanList = resp.languages();
            Iterator<DominantLanguage> lanIterator = allLanList.iterator();

            while (lanIterator.hasNext()) {
                DominantLanguage lang = lanIterator.next();
                lanCode = lang.languageCode();
            }

            return lanCode;

        } catch (ComprehendException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return "";
    }

    public String textTranslateToEn(String lanCode, String text) {

        Region region = Region.AP_NORTHEAST_2;
        TranslateClient translateClient = TranslateClient.builder()
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .region(region)
                .build();
        try {
            TranslateTextRequest textRequest = TranslateTextRequest.builder()
                    .sourceLanguageCode(lanCode)
                    .targetLanguageCode("kr")
                    .text(text)
                    .build();

            TranslateTextResponse textResponse = translateClient.translateText(textRequest);
            return textResponse.translatedText();

        } catch (TranslateException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";
    }


    public String textTranslateFromEn(String lanCode, String text) {

        Region region = Region.AP_NORTHEAST_2;
        TranslateClient translateClient = TranslateClient.builder()
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .region(region)
                .build();
        try {
            TranslateTextRequest textRequest = TranslateTextRequest.builder()
                    .sourceLanguageCode("kr")
                    .targetLanguageCode(lanCode)
                    .text(text)
                    .build();

            TranslateTextResponse textResponse = translateClient.translateText(textRequest);
            return textResponse.translatedText();

        } catch (TranslateException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";
    }
}