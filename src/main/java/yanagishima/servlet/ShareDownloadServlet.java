package yanagishima.servlet;

import static yanagishima.util.DownloadUtil.downloadTsv;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import yanagishima.service.PublishService;

@Api(tags = "share")
@RestController
@RequiredArgsConstructor
public class ShareDownloadServlet {
  private final PublishService publishService;

  @GetMapping("share/download")
  public void get(@RequestParam(name = "publish_id", required = false) String publishId,
                  @RequestParam(defaultValue = "UTF-8") String encode,
                  @RequestParam(defaultValue = "true") boolean header,
                  @RequestParam(defaultValue = "true") boolean bom,
                  HttpServletResponse response) {
    if (publishId == null) {
      return;
    }

    publishService.get(publishId).ifPresent(publish -> {
      String fileName = publishId + ".tsv";
      downloadTsv(response, fileName, publish.getDatasource(), publish.getQueryId(), encode, header, bom);
    });
  }
}
