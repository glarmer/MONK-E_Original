package com.lordnoisy.hoobabot;

import com.lordnoisy.hoobabot.utility.EmbedBuilder;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageCreateSpec;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URL;
import java.util.List;
import java.util.Random;

public class OCR {
    //TODO: DOES THIS EVEN WORK STILL?
    private final String tesseractDataPath = "tesseract/tessdata";
    Tesseract tesseract = new Tesseract();
    private String fileAddress = "";
    private String fileType = "";
    private ChatInputInteractionEvent event;
    private EmbedBuilder embeds;

    public OCR(ChatInputInteractionEvent event, String url, EmbedBuilder embeds) {
        tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_LSTM_ONLY);
        tesseract.setDatapath(tesseractDataPath);
        this.embeds = embeds;
        this.event = event;
        this.fileAddress = url;
    }

    public MessageCreateSpec doOCR() {
        String outputText = "";
        boolean hasTXT = false;
        //Check if there is an attached image and save the link if there is.
        //try {
        //    List<Attachment> attachments = event.getMessage().getAttachments();
        //    if (attachments.get(0).getContentType().get().contains("image")){
        //      fileAddress = attachments.get(0).getUrl();
        //      String[] splitByDot = fileAddress.split("\\.");
        //      this.fileType = "." + splitByDot[splitByDot.length - 1].split("\\?")[0];
        //    }
        //} catch (Exception e) {
        //    e.printStackTrace();
        //    try {
        //        fileAddress = event.getMessage().getEmbeds().get(0).getImage().get().getUrl();
        //    } catch (Exception e2) {
        //        e2.printStackTrace();
        //    }
        //}

        fileAddress = "https://www.srcmake.com/uploads/5/3/9/0/5390645/ocr_orig.png";
        this.fileType = ".png";
        try {
            String fileName = "ocr_"+randomString(10)+fileType;
            System.out.println("beaners" + fileName);
            File image = new File(fileName);
            FileUtils.copyURLToFile(
                    new URL(fileAddress),
                    image,
                    500,
                    10000);

            outputText = tesseract.doOCR(new File(fileName));
            System.out.println(outputText);
            image.delete();
        } catch (Exception e) {
            e.printStackTrace();
            outputText = "Error: Couldn't perform OCR on this image";
        }

        File text = null;
        if (outputText.length() > 6000) {
            try {
                text = new File("ocr_" + randomString(10) + ".txt");
                FileWriter fileWriter = new FileWriter(text);
                fileWriter.write(outputText);
                fileWriter.close();
                hasTXT = true;
                outputText = "This text is too long for discord, please see the attached image";
            } catch (Exception e) {
                outputText = "Error: Couldn't perform OCR on this image";
            }
        }

        MessageCreateSpec.Builder messageBuilder = MessageCreateSpec.builder()
                .addEmbed(embeds.getOCREmbed(this.event, outputText));

        try {
            if (hasTXT) {
                messageBuilder.addFile(MessageCreateFields.File.of(text.getName(), new FileInputStream(text)));
                //text.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageBuilder.build();
    }

    public String randomString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = length;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

}
