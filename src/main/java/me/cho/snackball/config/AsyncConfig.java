package me.cho.snackball.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        log.info("processors count {}", processors);
        executor.setCorePoolSize(processors); // 수영장의 튜브 개수 같은 것(이벤트)
        executor.setMaxPoolSize(processors * 2); // 줄 선 사람이 50명 넘으면 튜브를 새로 만들어서 준다
        executor.setQueueCapacity(50); // 남는 튜브 없을 때 줄 세우기
        executor.setKeepAliveSeconds(60); // 추가로 만든 튜브를 언제 수거해서 없앨거냐
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.initialize();
        return executor;
    }
}
