package com.vi.api;

import com.framework.model.Result;
import com.vi.entity.dto.APIURLLinkUploadDTO;
import com.vi.entity.vo.APIURLsInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 这是一个调用python微服务的openfeign接口
 *
 * @author dotm
 */
@FeignClient(name = "python-service", path = "/file")
public interface PythonFileController {

    /**
     * 访问python微服务
     */
    @PostMapping("/geturl")
    Result<List<APIURLsInfoVO>> getURLsByVideoURL(@RequestBody APIURLLinkUploadDTO url);
}
