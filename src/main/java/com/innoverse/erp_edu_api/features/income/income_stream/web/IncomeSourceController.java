package com.innoverse.erp_edu_api.features.income.income_stream.web;

import com.innoverse.erp_edu_api.features.income.income_stream.web.dto.CreateIncomeSourceRequest;
import com.innoverse.erp_edu_api.features.income.income_stream.web.dto.IncomeSourceDto;
import com.innoverse.erp_edu_api.features.income.income_stream.service.IncomeSourceFacade;
import com.innoverse.erp_edu_api.features.income.income_stream.web.dto.UpdateIncomeSourceRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/schools/income-sources")
public class IncomeSourceController {
    private final IncomeSourceFacade incomeSourceService;

    public IncomeSourceController(IncomeSourceFacade incomeSourceService) {
        this.incomeSourceService = incomeSourceService;
    }

    @PostMapping
    public ResponseEntity<Optional<IncomeSourceDto>> createIncomeSource(
            @Valid @RequestBody CreateIncomeSourceRequest request) {

        var created = incomeSourceService.createIncomeSource(request);
        return ResponseEntity.created(URI.create("/api/schools/income-sources/" + created.get().id()))
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeSourceDto> getIncomeSource(@PathVariable UUID id) {
        return incomeSourceService.getIncomeSourceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<IncomeSourceDto> getAllIncomeSources() {
        return incomeSourceService.getAllIncomeSources();
    }

    @GetMapping("/active")
    public List<IncomeSourceDto> getActiveIncomeSources() {
        return incomeSourceService.getActiveIncomeSources();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IncomeSourceDto> updateIncomeSource(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateIncomeSourceRequest request) {

        return incomeSourceService.updateIncomeSource(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncomeSource(@PathVariable UUID id) {
        return incomeSourceService.deleteIncomeSource(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

}