package om.dxline.dxtalent.communication.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import om.dxline.dxtalent.communication.domain.model.ChatRoom;
import om.dxline.dxtalent.communication.domain.model.ChatRoomId;
import om.dxline.dxtalent.communication.domain.repository.ChatRoomRepository;
import om.dxline.dxtalent.identity.domain.model.UserId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ChatRoom Repository Adapter (인프라 레이어)
 *
 * ChatRoomRepository 인터페이스의 구현체입니다.
 * Spring Data JPA를 사용하여 도메인 모델을 영속화합니다.
 *
 * 역할:
 * - 도메인 모델 ↔ JPA 엔티티 변환 (ChatRoomMapper 사용)
 * - JPA Repository를 통한 데이터베이스 접근
 * - 트랜잭션 관리
 *
 * 설계 원칙:
 * - 도메인 레이어는 이 클래스를 알지 못함 (DIP)
 * - 도메인 모델만 반환 (JPA Entity 노출 안 함)
 */
@Component
@Transactional(readOnly = true)
public class ChatRoomRepositoryAdapter implements ChatRoomRepository {

    private final ChatRoomJpaRepository jpaRepository;
    private final ChatRoomMapper mapper;

    public ChatRoomRepositoryAdapter(
        ChatRoomJpaRepository jpaRepository,
        ChatRoomMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ChatRoom save(ChatRoom chatRoom) {
        // 도메인 모델 → JPA Entity
        ChatRoomJpaEntity entity = mapper.toEntity(chatRoom);

        // DB 저장
        ChatRoomJpaEntity savedEntity = jpaRepository.save(entity);

        // JPA Entity → 도메인 모델
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ChatRoom> findById(ChatRoomId id) {
        if (id == null || id.getValue() == null) {
            return Optional.empty();
        }

        try {
            Long entityId = Long.parseLong(id.getValue().toString());
            return jpaRepository.findById(entityId).map(mapper::toDomain);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ChatRoom> findAll() {
        List<ChatRoomJpaEntity> entities = jpaRepository.findAll();
        return mapper.toDomainList(entities);
    }

    @Override
    public List<ChatRoom> findByParticipant(UserId userId) {
        if (userId == null) {
            return List.of();
        }

        // JSON 필드 검색을 위한 패턴
        // participants JSON에 "userId": 123 형태로 저장되므로
        String userIdPattern = "\"userId\":" + userId.getValue();

        List<ChatRoomJpaEntity> entities =
            jpaRepository.findByParticipantUserId(userIdPattern);
        return mapper.toDomainList(entities);
    }

    @Override
    public Optional<ChatRoom> findDirectChatBetweenUsers(
        UserId user1,
        UserId user2
    ) {
        if (user1 == null || user2 == null) {
            return Optional.empty();
        }

        // JSON 필드 검색을 위한 패턴
        String user1Pattern = "\"userId\":" + user1.getValue();
        String user2Pattern = "\"userId\":" + user2.getValue();

        Optional<ChatRoomJpaEntity> entityOpt =
            jpaRepository.findDirectChatBetweenUsers(
                ChatRoomTypeJpa.DIRECT,
                user1Pattern,
                user2Pattern
            );

        return entityOpt.map(mapper::toDomain);
    }

    @Override
    public List<ChatRoom> findGroupChatRoomsWithAllParticipants(
        List<UserId> userIds
    ) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        // 모든 GROUP 채팅방 조회
        List<ChatRoomJpaEntity> groupChatEntities = jpaRepository.findByType(
            ChatRoomTypeJpa.GROUP
        );

        // 도메인 모델로 변환
        List<ChatRoom> groupChats = mapper.toDomainList(groupChatEntities);

        // 모든 사용자가 참여 중인 채팅방만 필터링
        return groupChats
            .stream()
            .filter(chatRoom -> {
                // 모든 userIds가 참여자 목록에 포함되는지 확인
                return userIds.stream().allMatch(chatRoom::isParticipant);
            })
            .toList();
    }

    @Override
    @Transactional
    public void delete(ChatRoom chatRoom) {
        if (chatRoom == null || chatRoom.getId() == null) {
            return;
        }

        deleteById(chatRoom.getId());
    }

    @Override
    @Transactional
    public void deleteById(ChatRoomId id) {
        if (id == null || id.getValue() == null) {
            return;
        }

        try {
            Long entityId = Long.parseLong(id.getValue().toString());
            jpaRepository.deleteById(entityId);
        } catch (NumberFormatException e) {
            // ID 형식이 잘못된 경우 무시
        }
    }

    @Override
    public boolean existsById(ChatRoomId id) {
        if (id == null || id.getValue() == null) {
            return false;
        }

        try {
            Long entityId = Long.parseLong(id.getValue().toString());
            return jpaRepository.existsById(entityId);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
