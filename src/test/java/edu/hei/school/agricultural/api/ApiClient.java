package edu.hei.school.agricultural.api;

import edu.hei.school.agricultural.api.model.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.0.150:8080";
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ApiClient() {
        this.baseUrl = BASE_URL;
        this.restTemplate = getRestTemplate();
    }

    private RestTemplate getRestTemplate() {
        var template = new RestTemplate();
        template.setInterceptors(Collections.singletonList(apiKeyInterceptor()));
        return template;
    }

    private ClientHttpRequestInterceptor apiKeyInterceptor() {
        return (request, body, execution) -> {
            String key = "agri-secure-key";
            request.getHeaders().set("x-api-key", key);
            return execution.execute(request, body);
        };
    }

    // =========================
    // Collectivities
    // =========================

    public List<Collectivity> createCollectivities(List<CreateCollectivity> body) {
        return post("/collectivities", body, new ParameterizedTypeReference<>() {
        });
    }

    public Collectivity getCollectivity(String id) {
        return get("/collectivities/" + id, Collectivity.class);
    }

    public Collectivity updateCollectivityInformations(String id, CollectivityInformation informations) {
        return put("/collectivities/" + id + "/informations", informations, Collectivity.class);
    }

    public List<FinancialAccount> getCollectivityFinancialAccounts(String id) {
        return get("/collectivities/" + id + "/financialAccounts", new ParameterizedTypeReference<>() {
        });
    }

    public List<FinancialAccount> getCollectivityFinancialAccountsAt(String id, LocalDate at) {
        return get("/collectivities/" + id + "/financialAccounts?at=" + at, new ParameterizedTypeReference<>() {
        });
    }

    public List<MembershipFee> getCollectivityMembershipFees(String id) {
        return get("/collectivities/" + id + "/membershipFees", new ParameterizedTypeReference<>() {
        });
    }

    public List<MembershipFee> createCollectivityMembershipFees(String id, List<CreateMembershipFee> body) {
        return post("/collectivities/" + id + "/membershipFees", body, new ParameterizedTypeReference<>() {
        });
    }

    public List<CollectivityTransaction> getCollectivityTransactions(String id, LocalDate from, LocalDate to) {
        return get("/collectivities/" + id + "/transactions?from=" + from + "&to=" + to, new ParameterizedTypeReference<>() {
        });
    }

    public List<CollectivityLocalStatistics> getCollectivityStatistics(String id, LocalDate from, LocalDate to) {
        return get("/collectivities/" + id + "/statistics?from=" + from + "&to=" + to, new ParameterizedTypeReference<>() {
        });
    }

    public List<CollectivityOverallStatistics> getCollectivitiesOverallStatistics(LocalDate from, LocalDate to) {
        return get("/collectivites/statistics?from=" + from + "&to=" + to, new ParameterizedTypeReference<>() {
        });
    }

    public List<CollectivityActivity> getCollectivityActivities(String id) {
        return get("/collectivites/" + id + "/activities", new ParameterizedTypeReference<>() {
        });
    }

    public List<CollectivityActivity> createCollectivityActivities(String id, List<CreateCollectivityActivity> body) {
        return post("/collectivities/" + id + "/activities", body, new ParameterizedTypeReference<>() {
        });
    }

    public List<ActivityMemberAttendance> createActivityAttendance(String collectivityId, String activityId, List<CreateActivityMemberAttendance> body) {
        return post("/collectivities/" + collectivityId + "/activities/" + activityId + "/attendance", body, new ParameterizedTypeReference<>() {
        });
    }

    public List<ActivityMemberAttendance> getActivityAttendance(String collectivityId, String activityId) {
        return get("/collectivities/" + collectivityId + "/activities/" + activityId + "/attendance", new ParameterizedTypeReference<>() {
        });
    }

    // =========================
    // Members
    // =========================

    public List<Member> createMembers(List<CreateMember> body) {
        return post("/members", body, new ParameterizedTypeReference<>() {
        });
    }

    public List<MemberPayment> createMemberPayments(String memberId, List<CreateMemberPayment> body) {
        return post("/members/" + memberId + "/payments", body, new ParameterizedTypeReference<>() {
        });
    }

    // =========================
    // Low-level GET
    // =========================

    public <T> T get(String path, Class<T> responseType) {
        return exchange(path, HttpMethod.GET, null, responseType);
    }

    public <T> T get(String path, ParameterizedTypeReference<T> typeRef) {
        return exchange(path, HttpMethod.GET, null, typeRef);
    }

    // =========================
    // Low-level POST
    // =========================

    public <T> T post(String path, Object body, Class<T> responseType) {
        return exchange(path, HttpMethod.POST, body, responseType);
    }

    public <T> T post(String path, Object body, ParameterizedTypeReference<T> typeRef) {
        return exchange(path, HttpMethod.POST, body, typeRef);
    }

    // =========================
    // Low-level PUT
    // =========================

    public <T> T put(String path, Object body, Class<T> responseType) {
        return exchange(path, HttpMethod.PUT, body, responseType);
    }

    public <T> T put(String path, Object body, ParameterizedTypeReference<T> typeRef) {
        return exchange(path, HttpMethod.PUT, body, typeRef);
    }

    // =========================
    // Low-level DELETE
    // =========================

    public void delete(String path) {
        exchange(path, HttpMethod.DELETE, null, Void.class);
    }

    public <T> T delete(String path, ParameterizedTypeReference<T> typeRef) {
        return exchange(path, HttpMethod.DELETE, null, typeRef);
    }

    // =========================
    // Core
    // =========================

    private <T> T exchange(String path, HttpMethod method, Object body, Class<T> responseType) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    baseUrl + path, method, buildEntity(body), responseType);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw buildException(e);
        }
    }

    private <T> T exchange(String path, HttpMethod method, Object body, ParameterizedTypeReference<T> typeRef) {
        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    baseUrl + path, method, buildEntity(body), typeRef);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            throw buildException(e);
        }
    }

    private HttpEntity<?> buildEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private RuntimeException buildException(HttpStatusCodeException e) {
        return new RuntimeException(
                "HTTP Error: " + e.getStatusCode() + " | Body: " + e.getResponseBodyAsString(), e);
    }
}
