package passroutebackend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import passroutebackend.interview.entity.InterviewFormat;
import passroutebackend.interview.entity.InterviewType;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToInterviewTypeConverter());
        registry.addConverter(new StringToInterviewFormatConverter());
    }

    private static class StringToInterviewTypeConverter implements Converter<String, InterviewType> {
        @Override
        public InterviewType convert(String source) {
            for (InterviewType type : InterviewType.values()) {
                if (type.getValue().equalsIgnoreCase(source) || type.name().equalsIgnoreCase(source)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("유효하지 않은 면접 유형입니다: " + source);
        }
    }

    private static class StringToInterviewFormatConverter implements Converter<String, InterviewFormat> {
        @Override
        public InterviewFormat convert(String source) {
            for (InterviewFormat format : InterviewFormat.values()) {
                if (format.name().equalsIgnoreCase(source)) {
                    return format;
                }
            }
            throw new IllegalArgumentException("유효하지 않은 면접 형식입니다: " + source);
        }
    }
}
