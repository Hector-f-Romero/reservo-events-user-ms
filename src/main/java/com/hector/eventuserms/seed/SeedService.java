package com.hector.eventuserms.seed;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hector.eventuserms.events.EventRepository;
import com.hector.eventuserms.events.EventService;
import com.hector.eventuserms.events.dtos.request.CreateEventRequestDto;
import com.hector.eventuserms.seats.SeatRepository;
import com.hector.eventuserms.users.UserRepository;
import com.hector.eventuserms.users.models.User;

import io.github.cdimascio.dotenv.Dotenv;

@Service
public class SeedService {
        private final SeatRepository seatRepository;
        private final EventRepository eventRepository;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final EventService eventService;

        public SeedService(SeatRepository seatRepository, EventRepository eventRepository,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder, EventService eventService) {
                this.seatRepository = seatRepository;
                this.eventRepository = eventRepository;
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.eventService = eventService;

        }

        public String executeSeed() {

                // 1. Delete all existing data from the database.
                userRepository.deleteAll();
                eventRepository.deleteAll();
                seatRepository.deleteAll();

                // 2. Create default users.
                List<User> seedUsers = this.createSeedUsers();

                // 3. Create the default events using the previously seed users.
                this.createSeedEvents(seedUsers);

                return "Seed Executed";
        }

        private List<User> createSeedUsers() {

                Dotenv dotenv = Dotenv.load();

                // 1. Hash the password for seed users using the value defined in .env
                String hashedPassword = passwordEncoder.encode(dotenv.get("DEFAULT_PASSWORD_SEED_USERS"));

                // 2. Create the default users in DB.
                User user1 = userRepository.save(User.builder()
                                .name("Enrique Manos Tijeras")
                                .username("test1")
                                .email("test1@gmail.com")
                                .password(hashedPassword)
                                .build());

                User user2 = userRepository.save(User.builder()
                                .name("Pedro Manos Cuchillas")
                                .username("test2")
                                .email("test2@gmail.com")
                                .password(hashedPassword)
                                .build());

                return List.of(user1, user2);

        }

        private void createSeedEvents(List<User> seedUsers) {

                // 1. Get the current date and set a fixed time to be reused across events. This
                // events only can be from 8:00 to 17:00
                ZonedDateTime now = ZonedDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0);

                // 2. Define default seat labels. All seed events will have 15 seats in the
                // project context.
                List<String> defaultSeats = Arrays.asList(
                                "A11", "A12", "A13", "A14", "A15", "B11", "B12", "B13", "B14", "B15", "C11", "C12",
                                "C13", "C14",
                                "C15");

                // 3. Prepare the event data using DTOs. These will be passed to the event
                // service.
                var event1Raw = new CreateEventRequestDto("Tech Innovation Summit 2025",
                                "The Tech Innovation Summit 2025 is a premier gathering of thought leaders, innovators, and professionals from across the technology sector. Attendees will explore breakthrough developments in AI, blockchain, quantum computing, and robotics through a series of keynote talks, panel discussions, and hands-on workshops. The summit also includes networking sessions and a startup pitch competition that highlights the next generation of tech entrepreneurs.",
                                now.plusDays(1).toInstant(), (short) 15, seedUsers.get(0).getId(), defaultSeats);

                var event2Raw = new CreateEventRequestDto("Global Sustainability Forum",
                                "The Global Sustainability Forum brings together policymakers, scientists, environmental activists, and business leaders to discuss actionable solutions to today’s most pressing ecological challenges. The forum covers key topics such as renewable energy adoption, circular economy models, biodiversity conservation, and climate justice. Through workshops and roundtables, participants will collaborate on strategies to drive systemic change and promote a sustainable future.",
                                now.plusDays(1).plusHours(6).toInstant(), (short) 15, seedUsers.get(1).getId(),
                                defaultSeats);

                var event3Raw = new CreateEventRequestDto("Art & Design Expo",
                                "The Art & Design Expo is a vibrant showcase of contemporary artistic expression and design innovation. The event features curated exhibitions from emerging and established artists, interactive installations, and immersive digital art experiences. With talks from leading designers and creative technologists, the expo encourages dialogue between disciplines and invites attendees to explore the future of visual storytelling, fashion, and user experience design.",
                                now.plusDays(6).plusHours(2).plusMinutes(30).toInstant(), (short) 15,
                                seedUsers.get(0).getId(), defaultSeats);

                // 4. Create events using the service. The service will internally generate
                // seats for each event.
                eventService.create(event1Raw);
                eventService.create(event2Raw);
                eventService.create(event3Raw);
        }
}
