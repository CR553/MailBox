package cr553.task3.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiveMessage implements Serializable {
    String sendDate;
    String subject;
    String receviceAddress;
    String fromUser;
    String content;
    boolean seen;
}
