package cn.edu.fudan.violation.core.analyzer;

import cn.edu.fudan.violation.domain.enums.ToolEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author beethoven
 * @date 2021-03-01 14:46:03
 */
@Slf4j
@Component
public class AnalyzerFactory {

    private ApplicationContext applicationContext;

    public BaseAnalyzer createAnalyzer(String tool) {

        if (ToolEnum.SONAR.getType().equals(tool)) {
            return createSonarQubeBaseAnalyzer();
        } else if (ToolEnum.ESLINT.getType().equals(tool)) {
            return createEsLintBaseAnalyzer();
        } else if (ToolEnum.TSCANCODE.getType().equals(tool)) {
            return createTscanCodeAnalyzer();
        }

        log.error("tool name error,input tool name is {} !", tool);
        return null;
    }

    private BaseAnalyzer createSonarQubeBaseAnalyzer() {
        return applicationContext.getBean(SonarQubeBaseAnalyzer.class);
    }

    private BaseAnalyzer createEsLintBaseAnalyzer() {
        return applicationContext.getBean(EsLintBaseAnalyzer.class);
    }

    private BaseAnalyzer createTscanCodeAnalyzer() {
        return applicationContext.getBean(TscanCodeAnalyzer.class);
    }

    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
