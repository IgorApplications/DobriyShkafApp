package com.iapp.iapp_messenger.controllers.rest_controllers;

import com.iapp.iapp_messenger.dao.dto.ApiResponse;
import com.iapp.iapp_messenger.dao.hibernate.Family;
import com.iapp.iapp_messenger.dao.hibernate.FamilyRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/families-admin")
public class RESTFamilyAdminController {

    private final FamilyRepository familyRepository;

    public RESTFamilyAdminController(FamilyRepository familyRepository) {
        this.familyRepository = familyRepository;
    }

    /**
     * Получить ВСЮ таблицу.
     */
    @GetMapping("/all")
    public ApiResponse<List<Family>> getAllFamilies() {
        return ApiResponse.ok(familyRepository.findAll());
    }

    /**
     * Получить одну семью по id.
     */
    @GetMapping("/{id}")
    public ApiResponse<Family> getFamily(@PathVariable Long id) {

        Family family = familyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Family not found"));

        return ApiResponse.ok(family);
    }

    /**
     * Создать новую семью.
     */
    @PostMapping("/create")
    public ApiResponse<Family> createFamily(@RequestBody Family family) {

        // id генерирует hibernate автоматически
        family.setId(null);

        return ApiResponse.ok(
                familyRepository.save(family)
        );
    }

    /**
     * Полное редактирование семьи.
     */
    @PostMapping("/update")
    public ApiResponse<Family> updateFamily(@RequestBody Family family) {

        Long id = family.getId();

        if (id == null) {
            throw new IllegalArgumentException("Family id is null");
        }

        Family existing = familyRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Family with id=" + id + " not found"
                        )
                );

        return ApiResponse.ok(
                familyRepository.save(family)
        );
    }

    /**
     * Полностью перезаписать таблицу.
     *
     * delete all
     * insert all
     */
    @Transactional
    @PostMapping("/rewrite-all")
    public ApiResponse<String> rewriteAll(
            @RequestBody List<Family> families
    ) {

        familyRepository.deleteAll();
        familyRepository.flush();

        for (Family family : families) {
            family.setId(null);
        }

        familyRepository.saveAll(families);

        return ApiResponse.ok("Table rewritten");
    }

    /**
     * Удаление семьи.
     */
    @PostMapping("/delete/{id}")
    public ApiResponse<String> deleteFamily(@PathVariable Long id) {

        familyRepository.deleteById(id);

        return ApiResponse.ok("Deleted");
    }

    @GetMapping("/hi")
    public String test() {
        return "HELLO WORLD!!! FAMILY ADMIN HI!!!";
    }
}