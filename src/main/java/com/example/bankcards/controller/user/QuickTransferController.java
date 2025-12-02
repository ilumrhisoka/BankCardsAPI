package com.example.bankcards.controller.user;

import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.model.dto.user.QuickTransferDto;
import com.example.bankcards.model.entity.QuickTransfer;
import com.example.bankcards.model.entity.User;
import com.example.bankcards.repository.QuickTransferRepository;
import com.example.bankcards.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user/quick-transfers")
@RequiredArgsConstructor
@Tag(name = "Quick Transfers", description = "Manage favorite contacts")
public class QuickTransferController {

    private final QuickTransferRepository quickTransferRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<QuickTransferDto>> getAll(Authentication authentication) {
        List<QuickTransfer> list = quickTransferRepository.findByUserUsername(authentication.getName());
        List<QuickTransferDto> dtos = list.stream().map(qt -> {
            QuickTransferDto dto = new QuickTransferDto();
            dto.setId(qt.getId());
            dto.setName(qt.getName());
            dto.setCardNumber(qt.getCardNumber());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<QuickTransferDto> create(@RequestBody QuickTransferDto request, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        QuickTransfer qt = new QuickTransfer();
        qt.setUser(user);
        qt.setName(request.getName());
        qt.setCardNumber(request.getCardNumber());

        QuickTransfer saved = quickTransferRepository.save(qt);

        request.setId(saved.getId());
        return ResponseEntity.ok(request);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        QuickTransfer qt = quickTransferRepository.findById(id).orElse(null);
        if (qt != null && qt.getUser().getUsername().equals(authentication.getName())) {
            quickTransferRepository.delete(qt);
        }
        return ResponseEntity.ok().build();
    }
}