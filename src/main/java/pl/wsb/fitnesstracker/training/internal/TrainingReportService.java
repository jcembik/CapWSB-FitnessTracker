package pl.wsb.fitnesstracker.training.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.wsb.fitnesstracker.mail.api.EmailDto;
import pl.wsb.fitnesstracker.mail.api.EmailSender;
import pl.wsb.fitnesstracker.training.api.Training;
import pl.wsb.fitnesstracker.training.api.TrainingProvider;
import pl.wsb.fitnesstracker.user.api.User;
import pl.wsb.fitnesstracker.user.api.UserProvider;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class TrainingReportService {

    private final TrainingProvider trainingProvider;
    private final UserProvider userProvider;
    private final EmailSender emailSender;

    @Scheduled(cron = "0 0 0 * * MON") // Every Monday at midnight
    public void generateWeeklyReport() {
        log.info("Starting weekly training report generation...");
        LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);
        
        List<Training> allTrainings = trainingProvider.getAllTrainings();
        List<User> allUsers = userProvider.findAllUsers();

        for (User user : allUsers) {
            List<Training> userTrainings = allTrainings.stream()
                    .filter(training -> training.getUser().getId().equals(user.getId()))
                    .toList();

            List<Training> lastWeekTrainings = userTrainings.stream()
                    .filter(training -> {
                        LocalDate trainingDate = training.getStartTime().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        return trainingDate.isAfter(oneWeekAgo) || trainingDate.isEqual(oneWeekAgo);
                    })
                    .toList();

            log.info("User: {} {}, Trainings last week: {}", user.getFirstName(), user.getLastName(), lastWeekTrainings.size());
            
            sendReportEmail(user, userTrainings.size());
        }
        log.info("Weekly training report generation finished.");
    }

    private void sendReportEmail(User user, int totalTrainings) {
        String subject = "Weekly Training Report";
        String content = String.format("Hello %s,\n\nYou have registered %d trainings in total.\n\nKeep up the good work!", 
                                       user.getFirstName(), totalTrainings);
        
        EmailDto emailDto = new EmailDto(user.getEmail(), subject, content);
        emailSender.send(emailDto);
    }
}
