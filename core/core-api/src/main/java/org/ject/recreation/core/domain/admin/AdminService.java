package org.ject.recreation.core.domain.admin;

import lombok.RequiredArgsConstructor;
import org.ject.recreation.core.api.controller.request.BlockUserRequestDto;
import org.ject.recreation.core.api.controller.request.GameDeleteRequestDto;
import org.ject.recreation.core.api.controller.response.GetAllUserResponseDto;
import org.ject.recreation.core.api.controller.response.ReportGameDetailResponseDto;
import org.ject.recreation.core.api.controller.response.ReportGameResponseDto;
import org.ject.recreation.core.support.error.CoreException;
import org.ject.recreation.core.support.error.ErrorType;
import org.ject.recreation.core.support.response.PageResponseDto;
import org.ject.recreation.storage.db.core.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public PageResponseDto<ReportGameResponseDto> getReportedGames(int page) {
        Pageable pageable = PageRequest.of(page, 7);
//        return reportRepository.findAllForAdmin(pageable)
//                .map(ReportGameResponseDto::from);
        Page<ReportGameResponseDto> result =
                reportRepository.findAllForAdmin(pageable)
                        .map(ReportGameResponseDto::from);

        return PageResponseDto.of(result);
    }

    public ReportGameDetailResponseDto getReportedDetailGames(long reportId) {
        ReportEntity byId = reportRepository.findById(reportId)
                .orElseThrow(() -> new CoreException(ErrorType.GAME_NOT_FOUND));
        return ReportGameDetailResponseDto.from(byId);
    }

    public Void deleteReportedDetailGames(GameDeleteRequestDto gameDeleteRequestDto) {
        ReportStatus reportStatus = gameDeleteRequestDto.getReportStatus();
        if (!ReportStatus.isDelete(reportStatus)) {
            return null;
        }
        Long reportId = gameDeleteRequestDto.getReportId();
        reportRepository.deleteById(reportId);
        return null;
    }

    public PageResponseDto<GetAllUserResponseDto> getAllUsers(int page) {
        Pageable pageable = PageRequest.of(page, 7);
        Page<GetAllUserResponseDto> result = userRepository.findAll(pageable)
                .map(userEntity -> {
                    // UserEntity를 UserInfoDto로 변환
                    GetAllUserResponseDto.UserInfoDto userInfoDto = GetAllUserResponseDto.UserInfoDto.from(userEntity);
                    // 단일 UserInfoDto를 포함하는 리스트를 생성하여 GetAllUserResponseDto로 감쌈
                    return new GetAllUserResponseDto(List.of(userInfoDto));
                });
        return PageResponseDto.of(result);
    }

    public Void blockUser(BlockUserRequestDto blockUserRequestDto) {
        List<String> emailList = blockUserRequestDto.getBanList().stream()
                .map(BlockUserRequestDto.UserBanItem::getEmail)
                .toList();

        // 2. 한 번에 조회
        List<UserEntity> users = userRepository.findAllById(emailList);

        // 3. 빠른 조회를 위해 Map으로 변환 (Key: 이메일, Value: 엔티티)
        Map<String, UserEntity> userMap = users.stream()
                .collect(Collectors.toMap(UserEntity::getEmail, user -> user));

        // 4. DTO를 순회하며 해당 유저에게 사유 적용
        blockUserRequestDto.getBanList().forEach(item -> {
            UserEntity user = userMap.get(item.getEmail());
            if (user != null) {
                // 기본값 처리 (사유가 없을 경우)
                ReportReason reason = (item.getReason() == null || item.getReason().isBlank())
                        ? null : item.getReason();

                // 엔티티의 정지 메서드 호출 (사유 포함)
                user.blockUser(reason);
            }
        });
        return null;
    }


    public Void unBlockUser(BlockUserRequestDto blockUserRequestDto) {
        List<String> emailList = new ArrayList<>();
        blockUserRequestDto.getBanList().forEach(
                item -> {
                    emailList.add(item.getEmail());
                });
        List<UserEntity> allById = userRepository.findAllById(emailList);
        for (UserEntity userEntity : allById) {
            userEntity.unblockUser();
        }
        return null;
    }

}
