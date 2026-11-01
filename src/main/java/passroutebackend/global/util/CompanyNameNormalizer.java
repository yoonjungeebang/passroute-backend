package passroutebackend.global.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class CompanyNameNormalizer {

    private static final Pattern CORP_SUFFIX = Pattern.compile(
        "\\(주\\)|주식회사|inc\\.?|corp\\.?|ltd\\.?|llc\\.?|co\\.?",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern KEEP_ONLY = Pattern.compile("[^a-z0-9가-힣]");

    private CompanyNameNormalizer() {
    }

    public static String normalize(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            return "";
        }

        String result = Normalizer.normalize(companyName, Normalizer.Form.NFKC);
        result = result.toLowerCase();
        result = CORP_SUFFIX.matcher(result).replaceAll("");
        result = KEEP_ONLY.matcher(result).replaceAll("");
        return result;
    }
}
