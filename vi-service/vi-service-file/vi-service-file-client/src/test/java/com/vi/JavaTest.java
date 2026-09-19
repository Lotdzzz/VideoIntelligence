package com.vi;

import com.framework.service.RedisCacheService;
import com.vi.constants.FileConstants;
import com.vi.entity.model.SlicePartInfo;
import com.vi.entity.vo.VideoSliceMissionVo;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collection;
import java.util.List;

@SpringBootTest
public class JavaTest {

    @Resource
    private RedisCacheService redisCacheService;

    @Test
    public void test() {
        Collection<String> keys = redisCacheService.keys("slice:aa5da7f2-1f20-4993-a101-2f356fe03208_2025年终汇报视频.mp4:*");
        List<SlicePartInfo> cacheObject = redisCacheService.multiGetCacheObject(keys);
        System.out.println(cacheObject);
    }
}
