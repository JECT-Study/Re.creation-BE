package org.ject.recreation.core.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.ject.recreation.core.api.controller.request.BlockUserRequestDto;
import org.ject.recreation.core.api.controller.request.GameDeleteRequestDto;
import org.ject.recreation.core.api.controller.request.ReportGameRequestDto;
import org.ject.recreation.core.api.controller.response.ReportGameDetailResponseDto;
import org.ject.recreation.core.api.controller.response.ReportGameResponseDto;
import org.ject.recreation.core.domain.admin.AdminService;
import org.ject.recreation.core.domain.game.Game;
import org.ject.recreation.core.support.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/games")
    public ApiResponse<Page<ReportGameResponseDto>> getReportGames(
            @RequestParam(defaultValue = "0") int page) {
        return ApiResponse.success(adminService.getReportedGames(page));
    }

    @GetMapping("games/{reportId}")
    public ApiResponse<ReportGameDetailResponseDto> getReportGame(
            @PathVariable Long reportId
    ){
        return ApiResponse.success(adminService.getReportedDetailGames(reportId));
    }

    @PostMapping("/games/delete")
    public ApiResponse<Void> deleteReportGame(@RequestBody GameDeleteRequestDto gameDeleteRequestDto){
        return ApiResponse.success(adminService.deleteReportedDetailGames(gameDeleteRequestDto));
    }

//    @PostMapping("/users/block")
//    public void blockUser(@RequestBody BlockUserRequestDto blockUserRequestDto){
//        return adminService.blockUser(blockUserRequestDto);
//    }
}
