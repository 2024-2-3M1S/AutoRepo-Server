package org.autorepo.server.domain.label.controller;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.label.dto.request.LabelListRequestDto;
import org.autorepo.server.domain.label.dto.request.UploadLabalRequestDto;
import org.autorepo.server.domain.label.service.LabelService;
import org.autorepo.server.global.common.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/label")
@RestController
public class LabelController {

    private final LabelService labelService;

    @PostMapping("/upload")
    public ResponseEntity<SuccessResponse<?>> uploadLabels(
            @RequestBody UploadLabalRequestDto uploadLabalRequestDto) {
                labelService.uploadLabel(uploadLabalRequestDto);
        return SuccessResponse.created(null);
    }
}

