package assra.bahria.fyp.Common.Utils;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;

public class CookieManager {
    private static final CookieManager ourInstance = new CookieManager();

    public static CookieManager getInstance() {
        return ourInstance;
    }

    private CookieManager() {
    }

    ClearableCookieJar cookieJar;

    public ClearableCookieJar getCookieJar() {
        return cookieJar;
    }

    public void setCookieJar(ClearableCookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }






}