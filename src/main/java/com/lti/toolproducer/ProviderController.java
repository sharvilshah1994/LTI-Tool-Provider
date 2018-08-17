package com.lti.toolproducer;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.server.OAuthServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class ProviderController {

    private final Environment environment;

    @Autowired
    public ProviderController(Environment environment) {
        this.environment = environment;
    }

    @PostMapping("/lti")
    public String verifyUser(Model model, HttpServletRequest request) {
        Map<String, String> paramsMap = createMap(request);
        String validatedError = validateRequriedParams(paramsMap);
        if (validatedError != null) {
            model.addAttribute("error", validatedError);
            return "error";
        }
        if (validateSignature(request, paramsMap, model)) {
            checkIfGradeCallBackExists(paramsMap, model);
            return "producer-home";
        }
        return "error";
    }

    private void checkIfGradeCallBackExists(Map<String, String> paramsMap, Model model) {
        if (paramsMap.get("lis_outcome_service_url") != null) {
            String consumerUrl = paramsMap.get("lis_outcome_service_url");
            model.addAttribute("consumerUrl", consumerUrl);
            model.addAttribute("consumerUrlExists", false);
        } else {
            model.addAttribute("consumerUrlExists", true);
        }
    }

    private String validateRequriedParams(Map<String, String> paramsMap) {
        if (paramsMap.get("resource_link_id") == null) {
            return "No `resource_link_id` provided.";
        }
        if (paramsMap.get("lti_version") == null) {
            return "No `lti_version` provided.";
        }
        if (!paramsMap.get("lti_version").equals("LTI-1p0")) {
            return "Invalid LTI Version.";
        }
        if (paramsMap.get("lti_message_type") == null) {
            return "No `lti_message_type` provided.";
        }
        if (!paramsMap.get("lti_message_type").equals("basic-lti-launch-request")) {
            return "Invalid LTI Message Type";
        }
        if (paramsMap.get("lis_person_name_given") == null &&
                paramsMap.get("lis_person_contact_email_primary") != null) {
            return "Email provided but name not provided";
        }
        if (paramsMap.get("lis_person_name_given") != null && paramsMap.get("lis_person_name_family") != null &&
                paramsMap.get("lis_person_name_full") == null) {
            return "Full name not provided; given & family names provided";
        }
        return null;
    }

    private Boolean validateSignature(HttpServletRequest request, Map<String, String> paramsMap, Model model) {
        String consumerKey = paramsMap.get("oauth_consumer_key");
        Optional<String> secretKey = Optional.ofNullable(environment.getProperty(consumerKey));
        if (!secretKey.isPresent()) {
            model.addAttribute("error", "Invalid Consumer Key");
            return false;
        }
        OAuthMessage oam = OAuthServlet.getMessage(request, OAuthServlet.getRequestURL(request));

        OAuthValidator oav = new SimpleOAuthValidator();
        OAuthConsumer cons = new OAuthConsumer(null, consumerKey, secretKey.get(), null);
        OAuthAccessor acc = new OAuthAccessor(cons);

        try {
            oav.validateMessage(oam, acc);
        } catch (Exception var9) {
            model.addAttribute("error", "OAuth Sign Verification Failed");
            return false;
        }

        return true;
    }

    private Map<String, String> createMap(HttpServletRequest request) {
        HashMap<String, String> ltiParamHeaders = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            for (String paramValue : paramValues) {
                ltiParamHeaders.put(paramName, paramValue);
            }
        }
        return ltiParamHeaders;
    }
}
