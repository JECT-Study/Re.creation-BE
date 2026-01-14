package org.ject.recreation.core.domain.admin;

import lombok.RequiredArgsConstructor;
import org.ject.recreation.core.api.controller.request.BlockUserRequestDto;
import org.ject.recreation.core.api.controller.request.GameDeleteRequestDto;
import org.ject.recreation.core.api.controller.response.ReportGameDetailResponseDto;
import org.ject.recreation.core.api.controller.response.ReportGameResponseDto;
import org.ject.recreation.core.support.error.CoreException;
import org.ject.recreation.core.support.error.ErrorType;
import org.ject.recreation.core.support.response.PageResponseDto;
import org.ject.recreation.storage.db.core.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AdminService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public Page<ReportGameResponseDto> getReportedGames(int page) {
        Pageable pageable = PageRequest.of(page, 7);
        return reportRepository.findAllForAdmin(pageable)
                .map(ReportGameResponseDto::from);
    }

    public ReportGameDetailResponseDto getReportedDetailGames(long reportId) {
        ReportEntity byId = reportRepository.findById(reportId)
                .orElseThrow(() -> new CoreException(ErrorType.GAME_NOT_FOUND));
        return ReportGameDetailResponseDto.from(byId);
    }

    public Void deleteReportedDetailGames(GameDeleteRequestDto gameDeleteRequestDto) {
        ReportStatus reportStatus = gameDeleteRequestDto.getReportStatus();
        if(!ReportStatus.isDelete(reportStatus)){
            return null;
        }
        Long reportId = gameDeleteRequestDto.getReportId();
        reportRepository.deleteById(reportId);
        return null;
    }

//    public Void blockUser(BlockUserRequestDto blockUserRequestDto){
//        List<UserEntity> allById = userRepository.findAllById(blockUserRequestDto.getEmail());
//        allById.
//    }

}
