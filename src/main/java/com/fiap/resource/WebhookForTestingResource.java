package com.fiap.resource;

import com.fiap.dto.ResponseData;

import io.quarkus.logging.Log;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/webhook")
public class WebhookForTestingResource {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response webhook(ResponseData responseData) {
        Log.info("============================================");
        Log.info("Received response data: " + responseData.getEstado());
        return Response.ok("Webhook Enviado com sucesso").build();
    }
}
