package om.dxline.dxtalent.communication.domain.model;

import om.dxline.dxtalent.shared.domain.model.BaseValueObject;

public class MessageId extends BaseValueObject {
    private final Long value;

    public MessageId(Long value) {
        this.value = requireNonNull(value, "Message ID");
    }

    public static MessageId newId() {
        return new MessageId(System.currentTimeMillis());
    }

    public static MessageId of(Long id) {
        return new MessageId(id);
    }

    public Long getValue() {
        return value;
    }

    @Override
    protected Object[] getEqualityComponents() {
        return new Object[] { value };
    }
}
