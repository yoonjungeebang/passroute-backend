package passroutebackend.global.sms;

public interface SmsService {
    void sendSms(String to, String message);
}
