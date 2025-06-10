package com.fiap.services;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProcessadorService {

    public void processarVideo() throws Exception, IOException {
        FileOutputStream fos = new FileOutputStream(new File("outputs/video-frames.zip"));
        // Quando for repassar o .zip, alterar o ZipOutputStream(fos) para o
        // ZipOutputStream(os) para usar ByteArrayOutputStream
        // Assim vc repassa os bytes e não salva o arquivo no disco
        // colocar o "os" e o "zipOutputStream" fora
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(fos)) {

            FFmpegFrameGrabber g = new FFmpegFrameGrabber("testevideo.mp4");
            g.start();

            Java2DFrameConverter converter = new Java2DFrameConverter();
            Frame frame;
            Integer i = 0;

            while ((frame = g.grabImage()) != null) {
                BufferedImage bi = converter.convert(frame);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                // ZipOutputStream zipOutputStream2 = new ZipOutputStream(os);
                ImageIO.write(bi, "jpeg", os);

                ZipEntry zipEntry = new ZipEntry("testevideo-" + i + ".png");
                zipEntry.setSize(os.size());
                zipOutputStream.putNextEntry(zipEntry);
                zipOutputStream.write(os.toByteArray());

                zipOutputStream.closeEntry();

                i++;
            }

            g.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fos.close();
        }
    }
}