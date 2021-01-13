package id.ten.simple.otpauthentication.api;

import id.ten.simple.otpauthentication.service.EmailService;
import id.ten.simple.otpauthentication.service.OTPService;
import id.ten.simple.otpauthentication.template.EmailTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class OTPController {

    @Autowired
    private OTPService otpService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/generateOtp")
    public String generateOTP() throws MessagingException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        int otp = otpService.generateOTP(username);
        //Generate The Template to send OTP
        EmailTemplate template = new EmailTemplate("src/main/java/SendOtp.html");
        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("user", username);
        replacements.put("otpnum", String.valueOf(otp));

        String message = "Your OTP is : " + otp;

        emailService.sendOtpMessage("teten@mail.com", "OTP -SpringBoot", message);

        return "otppage";
    }

    @RequestMapping(value = "/validateOtp", method = RequestMethod.GET)
    public @ResponseBody
    String validateOtp(@RequestParam("otpnum") int otpnum) {

        final String SUCCESS = "Entered Otp is valid";
        final String FAIL = "Entered Otp is NOT valid. Please Retry!";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        //Validate the Otp
        if (otpnum >= 0) {

            int serverOtp = otpService.getOtp(username);
            if (serverOtp > 0) {
                if (otpnum == serverOtp) {
                    otpService.clearOTP(username);

                    return ("Entered Otp is valid");
                } else {
                    return SUCCESS;
                }
            } else {
                return FAIL;
            }
        } else {
            return FAIL;
        }
    }
}
