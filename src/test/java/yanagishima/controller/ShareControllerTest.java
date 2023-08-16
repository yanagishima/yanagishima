package yanagishima.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static yanagishima.TestHelper.getPublishId;
import static yanagishima.TestHelper.getQueryId;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import yanagishima.model.db.Publish;
import yanagishima.repository.PublishRepository;
import yanagishima.util.DownloadUtil;

@SpringBootTest
@AutoConfigureMockMvc
class ShareControllerTest {
  private static final String SHARE_CSV_DOWNLOAD = "/share/csvdownload";

  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private PublishRepository publishRepository;

  @Test
  void getCsv() throws Exception {
    String publishId = getPublishId();

    Publish publish = new Publish();
    publish.setPublishId(publishId);
    publish.setQueryId(getQueryId());
    publish.setUserid("Alice");

    try (MockedStatic<DownloadUtil> util = Mockito.mockStatic(DownloadUtil.class)) {
      util.when(() -> DownloadUtil.downloadCsv(
          any(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyBoolean()))
          .thenAnswer((Answer<Void>) invocation -> null);

      when(publishRepository.findByPublishId(anyString()))
          .thenReturn(Optional.of(publish));

      mockMvc
          .perform(get(SHARE_CSV_DOWNLOAD)
                       .param("publish_id", publishId)
                       .header("empno", "Alice"))
          .andDo(print())
          .andExpect(status().isOk());
    }
  }

  @Test
  void getCsvByViewers() throws Exception {
    String publishId = getPublishId();

    Publish publish = new Publish();
    publish.setPublishId(publishId);
    publish.setQueryId(getQueryId());
    publish.setUserid("Alice");
    publish.setViewers("Bob");

    try (MockedStatic<DownloadUtil> util = Mockito.mockStatic(DownloadUtil.class)) {
      util.when(() -> DownloadUtil.downloadCsv(
          any(), anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyBoolean()))
          .thenAnswer((Answer<Void>) invocation -> null);

      when(publishRepository.findByPublishId(anyString()))
          .thenReturn(Optional.of(publish));

      mockMvc
          .perform(get(SHARE_CSV_DOWNLOAD)
                       .param("publish_id", publishId)
                       .header("empno", "Bob"))
          .andDo(print())
          .andExpect(status().isOk());
    }
  }

  @Test
  void getCsvByDifferentUserThenForbidden() throws Exception {
    String publishId = getPublishId();

    Publish publish = new Publish();
    publish.setPublishId(publishId);
    publish.setQueryId(getQueryId());
    publish.setUserid("Alice");

    when(publishRepository.findByPublishId(anyString()))
        .thenReturn(Optional.of(publish));

    mockMvc
        .perform(get(SHARE_CSV_DOWNLOAD)
                     .param("publish_id", publishId)
                     .header("empno", "Bob"))
        .andDo(print())
        .andExpect(status().isForbidden());
  }

  @Test
  void getThenForbidden() throws Exception {
    Publish publish = new Publish();

    when(publishRepository.findByPublishId(anyString()))
        .thenReturn(Optional.of(publish));

    mockMvc
        .perform(get(SHARE_CSV_DOWNLOAD)
                     .param("publish_id", getQueryId()))
        .andDo(print())
        .andExpect(status().isForbidden());
  }
}
