package de.caritas.cob.liveservice.controller;

import static de.caritas.cob.liveservice.api.model.EventType.DIRECTMESSAGE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.caritas.cob.liveservice.api.controller.LiveController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@WebMvcTest(LiveController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LiveControllerIT {

  private static final String LIVEEVENT_SEND = "/liveevent/send";
  private static final String USER_IDS_PARAM = "userIds";
  public static final String EVENT_TYPE_PARAM = "eventType";

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void sendLiveEvent_Should_returnStatusOk_When_calledWithValidParams() throws Exception {
    mockMvc.perform(post(LIVEEVENT_SEND)
        .param(USER_IDS_PARAM, "1", "2").contentType(APPLICATION_JSON)
        .param(EVENT_TYPE_PARAM, DIRECTMESSAGE.toString())
        .contentType(APPLICATION_FORM_URLENCODED))
        .andExpect(status().isOk());
  }

  @Test
  public void sendLiveEvent_Should_returnBadRequest_When_userIdsAreMissing() throws Exception {
    mockMvc.perform(post(LIVEEVENT_SEND)
        .param(EVENT_TYPE_PARAM, DIRECTMESSAGE.toString())
        .contentType(APPLICATION_FORM_URLENCODED))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void sendLiveEvent_Should_returnBadRequest_When_eventTypeIsMissing() throws Exception {
    mockMvc.perform(post(LIVEEVENT_SEND)
        .param(USER_IDS_PARAM, "1", "2").contentType(APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

}
