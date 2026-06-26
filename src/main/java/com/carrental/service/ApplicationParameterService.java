package com.carrental.service;

import com.carrental.model.ApplicationParameter;
import com.carrental.model.ApplicationParameterCode;
import com.carrental.repository.ApplicationParameterRepository;
import com.carrental.web.dto.ApplicationParameterForm;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ApplicationParameterService {

    private final ApplicationParameterRepository applicationParameterRepository;

    public ApplicationParameterService(ApplicationParameterRepository applicationParameterRepository) {
        this.applicationParameterRepository = applicationParameterRepository;
    }

    @PostConstruct
    @Transactional
    public void ensureDefaults() {
        ensureDefault(
                ApplicationParameterCode.TOP_CUSTOMERS_LIMIT,
                "Maximum number of top customers shown on the dashboard",
                "5");
        ensureDefault(
                ApplicationParameterCode.LOW_UTILIZATION_THRESHOLD,
                "Fleet utilization percentage below which a vehicle is flagged as low performing",
                "20");
        applicationParameterRepository.findAll().forEach(parameter -> {
            if (parameter.getDisplayed() == null) {
                parameter.setDisplayed(Boolean.TRUE);
                applicationParameterRepository.save(parameter);
            }
        });
    }

    public List<ApplicationParameter> findAllOrdered() {
        return applicationParameterRepository.findAll(Sort.by("code"));
    }

    public ApplicationParameter getByCode(String code) {
        ApplicationParameter parameter = applicationParameterRepository.findByCode(code);
        if (parameter == null) {
            throw new IllegalArgumentException("Application parameter not found: " + code);
        }
        return parameter;
    }

    public ApplicationParameterForm toForm(ApplicationParameter parameter) {
        ApplicationParameterForm form = new ApplicationParameterForm();
        form.setCode(parameter.getCode());
        form.setDescription(parameter.getDescription());
        form.setValue(parameter.getValue());
        return form;
    }

    @Transactional
    @CacheEvict(value = "applicationParameterByCode", key = "#code")
    public void update(String code, ApplicationParameterForm form) {
        ApplicationParameter parameter = getByCode(code);
        parameter.setDescription(form.getDescription().trim());
        parameter.setValue(form.getValue().trim());
        applicationParameterRepository.save(parameter);
    }

    public int passInt(String code) {
        return Integer.parseInt(getAppParamValueByCode(code));
    }

    @Cacheable(value = "applicationParameterByCode", key = "#code")
    public String getAppParamValueByCode(String code) {
        ApplicationParameter applicationParameter = applicationParameterRepository.findByCode(code);
        if (applicationParameter != null) {
            return applicationParameter.getValue();
        }
        return null;
    }

    private void ensureDefault(ApplicationParameterCode code, String description, String value) {
        if (applicationParameterRepository.findByCode(code.name()) != null) {
            return;
        }
        ApplicationParameter parameter = new ApplicationParameter();
        parameter.setCode(code.name());
        parameter.setDescription(description);
        parameter.setValue(value);
        parameter.setDisplayed(Boolean.TRUE);
        applicationParameterRepository.save(parameter);
    }
}
