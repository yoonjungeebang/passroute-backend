package passroutebackend.auth.sms;

public interface SmsService {
    void sendSms(String to, String message);
}