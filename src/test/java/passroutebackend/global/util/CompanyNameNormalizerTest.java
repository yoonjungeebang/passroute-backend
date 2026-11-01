package passroutebackend.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CompanyNameNormalizerTest {

    @ParameterizedTest
    @DisplayName("크롤러 정규화 규칙과 동일하게 변환한다")
    @CsvSource({
        "(주)카카오, 카카오",
        "주식회사 네이버, 네이버",
        "NAVER Corp., naver",
        "토 스, 토스",
        "LG CNS, lgcns",
        "SK Inc., sk",
        "삼성전자 주식회사, 삼성전자",
        "LINE Corp, line",
        "쿠팡 LLC, 쿠팡",
    })
    void normalize(String input, String expected) {
        assertThat(CompanyNameNormalizer.normalize(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("null이나 빈 문자열은 빈 문자열을 반환한다")
    void normalizeNullOrBlank() {
        assertThat(CompanyNameNormalizer.normalize(null)).isEmpty();
        assertThat(CompanyNameNormalizer.normalize("")).isEmpty();
        assertThat(CompanyNameNormalizer.normalize("   ")).isEmpty();
    }
}
