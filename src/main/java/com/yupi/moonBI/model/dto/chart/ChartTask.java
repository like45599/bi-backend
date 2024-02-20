package com.yupi.moonBI.model.dto.chart;

import com.yupi.moonBI.model.entity.User;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;

@Data
public class ChartTask implements Serializable {
    private Long id;
    private byte[] fileData;
    private String fileName;
    private String contentType;

    private GenChartByAiRequest genChartByAiRequest;

    private User user;

    private static final long serialVersionUID = 1L;

    public void setMultipartFile(MultipartFile multipartFile) throws IOException {
        this.fileData = multipartFile.getBytes();
        this.fileName = multipartFile.getOriginalFilename();
        this.contentType = multipartFile.getContentType();
    }

}
