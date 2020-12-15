package yanagishima.servlet;

import static yanagishima.util.DownloadUtil.downloadCsv;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import yanagishima.repository.TinyOrm;

@Api(tags = "download")
@RestController
@RequiredArgsConstructor
public class ShareCsvDownloadServlet {
  private final TinyOrm db;

  @GetMapping("share/csvdownload")
  public void get(@RequestParam(name = "publish_id", required = false) String publishId,
                  @RequestParam(defaultValue = "UTF-8") String encode,
                  @RequestParam(defaultValue = "true") boolean header,
                  @RequestParam(defaultValue = "true") boolean bom,
                  HttpServletResponse response) {
    if (publishId == null) {
      return;
    }

    db.singlePublish("publish_id=?", publishId).ifPresent(publish -> {
      String fileName = publishId + ".csv";
      downloadCsv(response, fileName, publish.getDatasource(), publish.getQueryId(), encode, header, bom);
    });
  }
}
