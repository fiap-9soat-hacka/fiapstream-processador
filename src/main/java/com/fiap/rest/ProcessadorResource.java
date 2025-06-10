package com.fiap.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.xml.sax.SAXException;

import com.fiap.services.ProcessadorService;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/processador")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.TEXT_PLAIN)
public class ProcessadorResource {

    @Inject
    ProcessadorService processadorService;

    public static class VideoUploadForm {
        @RestForm("files")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        public List<InputStream> files;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void uploadVideos(VideoUploadForm form) throws IOException, SAXException, TikaException, Exception {
        for (InputStream video : form.files) {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();

            parser.parse(video, handler, metadata);
            Log.info(metadata.get(Metadata.CONTENT_TYPE));
        }

        processadorService.processarVideo();
    }
}
