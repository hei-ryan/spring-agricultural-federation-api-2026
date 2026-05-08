package edu.hei.school.agricultural.service;

import edu.hei.school.agricultural.api.ApiClient;
import edu.hei.school.agricultural.api.model.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ActivityAttendanceIT {

    final ApiClient apiClient = new ApiClient();

    // ID partagé entre les tests ordonnés (créé en test n°1, réutilisé ensuite)
    static String createdActivityId;

    // ================================================================
    // Création de l'activité ponctuelle
    // ================================================================

    @Test
    @Order(1)
    void create_punctual_meeting_activity_for_col1_ok() {
        // Contexte col-1 (Tableau 21) :
        //   act-1 : AG1, MEETING, toutes occupations, récurrent 1er samedi du mois
        //   act-2 : Formation de base, TRAINING, [JUNIOR], récurrent 2e dimanche
        // Nouvelle activité cohérente : même type MEETING, mêmes occupations qu'act-1,
        // mais ponctuelle (executiveDate seul, pas de recurrenceRule).
        var activity = new CreateCollectivityActivity();
        activity.label = "Réunion extraordinaire";
        activity.activityType = ActivityType.MEETING;
        activity.memberOccupationConcerned = List.of(
                MemberOccupation.JUNIOR,
                MemberOccupation.SENIOR,
                MemberOccupation.SECRETARY,
                MemberOccupation.TREASURER,
                MemberOccupation.VICE_PRESIDENT,
                MemberOccupation.PRESIDENT
        );
        activity.executiveDate = LocalDate.of(2026, 5, 10);
        // recurrenceRule non fournie → activité ponctuelle

        var result = apiClient.createCollectivityActivities("col-1", List.of(activity));

        assertNotNull(result);
        assertEquals(1, result.size(), "Une seule activité soumise → une seule retournée");
        log.info("Activité ponctuelle créée : " + result);

        var created = result.get(0);
        assertNotNull(created.id, "L'activité doit avoir un identifiant généré");
        assertEquals("Réunion extraordinaire", created.label);
        assertEquals(ActivityType.MEETING, created.activityType);
        assertNotNull(created.memberOccupationConcerned);
        assertTrue(created.memberOccupationConcerned.containsAll(List.of(
                MemberOccupation.PRESIDENT,
                MemberOccupation.JUNIOR,
                MemberOccupation.TREASURER)));
        assertEquals(LocalDate.of(2026, 5, 10), created.executiveDate);
        assertNull(created.recurrenceRule, "Activité ponctuelle : recurrenceRule doit être null");

        createdActivityId = created.id;
        log.info("ID de l'activité ponctuelle : " + createdActivityId);
    }

    // ================================================================
    // Récupération des activités de col-1
    // ================================================================

    @Test
    @Order(2)
    void get_col1_activities_contains_existing_recurring_and_new_punctual() {
        var activities = apiClient.getCollectivityActivities("col-1");

        assertNotNull(activities);
        assertTrue(activities.size() >= 3,
                "col-1 doit contenir au moins act-1, act-2 et la Réunion extraordinaire");
        log.info("Activités de col-1 : " + activities);

        // act-1 (AG1) : récurrent, sans executiveDate
        var ag1 = activities.stream().filter(a -> "AG1".equals(a.label)).findFirst();
        assertTrue(ag1.isPresent(), "act-1 AG1 doit toujours être présent");
        assertNotNull(ag1.get().recurrenceRule, "AG1 doit avoir une règle de récurrence");
        assertNull(ag1.get().executiveDate, "AG1 ne doit pas avoir d'executiveDate");
        assertEquals(ActivityType.MEETING, ag1.get().activityType);
        assertTrue(ag1.get().memberOccupationConcerned.contains(MemberOccupation.PRESIDENT));

        // act-2 (Formation de base) : récurrent, pour [JUNIOR]
        var formation = activities.stream().filter(a -> "Formation de base".equals(a.label)).findFirst();
        assertTrue(formation.isPresent(), "act-2 Formation de base doit toujours être présent");
        assertNotNull(formation.get().recurrenceRule);
        assertNull(formation.get().executiveDate);
        assertTrue(formation.get().memberOccupationConcerned.contains(MemberOccupation.JUNIOR));

        // Nouvelle activité ponctuelle : executiveDate 10/05/2026, sans recurrenceRule
        var reunionExtraordinaire = activities.stream()
                .filter(a -> "Réunion extraordinaire".equals(a.label))
                .findFirst();
        assertTrue(reunionExtraordinaire.isPresent(), "La Réunion extraordinaire doit être présente");
        assertEquals(LocalDate.of(2026, 5, 10), reunionExtraordinaire.get().executiveDate);
        assertNull(reunionExtraordinaire.get().recurrenceRule,
                "L'activité ponctuelle ne doit pas avoir de recurrenceRule");
        assertEquals(ActivityType.MEETING, reunionExtraordinaire.get().activityType);
    }

    // ================================================================
    // Création des présences
    // ================================================================

    @Test
    @Order(3)
    void create_attendance_for_all_original_col1_members_ok() {
        assertNotNull(createdActivityId,
                "L'ID de l'activité doit avoir été défini lors du test n°1");

        // Toutes les occupations étant concernées, tous les membres originaux sont requis.
        // Pattern inspiré des Tableaux 24/25 (act-1 mars et avril 2026) :
        //   Tableau 24 mars  : M1-M6 PRESENT, M7-M8 ABSENT
        //   Tableau 25 avril : M1-M2/M5-M8 PRESENT, M3-M4 ABSENT
        // Ici : M1/M2/M3/M5/M6/M8 ATTENDED, M4/M7 MISSING
        var attendances = List.of(
                attendance("C1-M1", AttendanceStatus.ATTENDED),
                attendance("C1-M2", AttendanceStatus.ATTENDED),
                attendance("C1-M3", AttendanceStatus.ATTENDED),
                attendance("C1-M4", AttendanceStatus.MISSING),
                attendance("C1-M5", AttendanceStatus.ATTENDED),
                attendance("C1-M6", AttendanceStatus.ATTENDED),
                attendance("C1-M7", AttendanceStatus.MISSING),
                attendance("C1-M8", AttendanceStatus.ATTENDED)
        );

        var result = apiClient.createActivityAttendance("col-1", createdActivityId, attendances);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        log.info("Présences enregistrées : " + result);

        assertStatus(result, "C1-M1", AttendanceStatus.ATTENDED);
        assertStatus(result, "C1-M2", AttendanceStatus.ATTENDED);
        assertStatus(result, "C1-M3", AttendanceStatus.ATTENDED);
        assertStatus(result, "C1-M4", AttendanceStatus.MISSING);
        assertStatus(result, "C1-M5", AttendanceStatus.ATTENDED);
        assertStatus(result, "C1-M6", AttendanceStatus.ATTENDED);
        assertStatus(result, "C1-M7", AttendanceStatus.MISSING);
        assertStatus(result, "C1-M8", AttendanceStatus.ATTENDED);
    }

    // ================================================================
    // Lecture de la fiche de présence complète
    // ================================================================

    @Test
    @Order(4)
    void get_attendance_includes_confirmed_members_and_undefined_new_members() {
        assertNotNull(createdActivityId);

        var attendances = apiClient.getActivityAttendance("col-1", createdActivityId);

        assertNotNull(attendances);
        assertFalse(attendances.isEmpty());
        log.info("Fiche de présence complète : " + attendances);

        // Les 8 membres originaux doivent avoir leur statut confirmé conservé
        assertStatus(attendances, "C1-M1", AttendanceStatus.ATTENDED);
        assertStatus(attendances, "C1-M2", AttendanceStatus.ATTENDED);
        assertStatus(attendances, "C1-M3", AttendanceStatus.ATTENDED);
        assertStatus(attendances, "C1-M4", AttendanceStatus.MISSING);
        assertStatus(attendances, "C1-M5", AttendanceStatus.ATTENDED);
        assertStatus(attendances, "C1-M6", AttendanceStatus.ATTENDED);
        assertStatus(attendances, "C1-M7", AttendanceStatus.MISSING);
        assertStatus(attendances, "C1-M8", AttendanceStatus.ATTENDED);

        // Les nouveaux adhérents JUNIOR (Tableau 18) dont la présence n'a pas été soumise
        // doivent apparaître en UNDEFINED : au moins 2 ont rejoint col-1 le 01/04/2026,
        // soit avant la date d'exécution du 10/05/2026.
        long undefinedCount = attendances.stream()
                .filter(a -> AttendanceStatus.UNDEFINED == a.attendanceStatus)
                .count();
        assertTrue(undefinedCount >= 2,
                "Au moins 2 nouveaux adhérents JUNIOR (Tableau 18, 01/04/2026) attendus en UNDEFINED ; obtenu : "
                        + undefinedCount);

        // La fiche totale dépasse les 8 membres originaux confirmés
        assertTrue(attendances.size() >= 10,
                "La fiche doit inclure 8 membres confirmés + les membres UNDEFINED ; taille obtenue : "
                        + attendances.size());
    }

    // ================================================================
    // Contrainte — immutabilité des statuts ATTENDED et MISSING
    // ================================================================

    @Test
    @Order(5)
    void cannot_update_attended_to_missing_once_confirmed() {
        assertNotNull(createdActivityId);

        // C1-M1 est ATTENDED → toute tentative de modification vers MISSING doit échouer en 400
        var update = List.of(attendance("C1-M1", AttendanceStatus.MISSING));

        var exception = assertThrows(RuntimeException.class,
                () -> apiClient.createActivityAttendance("col-1", createdActivityId, update));

        log.info("Erreur attendue (ATTENDED → MISSING) : " + exception.getMessage());
        assertTrue(exception.getMessage().contains("HTTP Error: 400"),
                "Modifier un statut ATTENDED déjà confirmé doit retourner 400");
    }

    @Test
    @Order(6)
    void cannot_update_missing_to_attended_once_confirmed() {
        assertNotNull(createdActivityId);

        // C1-M4 est MISSING → toute tentative de modification vers ATTENDED doit échouer en 400
        var update = List.of(attendance("C1-M4", AttendanceStatus.ATTENDED));

        var exception = assertThrows(RuntimeException.class,
                () -> apiClient.createActivityAttendance("col-1", createdActivityId, update));

        log.info("Erreur attendue (MISSING → ATTENDED) : " + exception.getMessage());
        assertTrue(exception.getMessage().contains("HTTP Error: 400"),
                "Modifier un statut MISSING déjà confirmé doit retourner 400");
    }

    // ================================================================
    // Contrainte — création d'activité invalide
    // ================================================================

    @Test
    @Order(7)
    void create_activity_with_executive_date_and_recurrence_rule_simultaneously_ko() {
        // La spec interdit de fournir executiveDate ET recurrenceRule simultanément.
        // "Note that either executive date or recurrence rule can be provided at same time, not both."
        var invalid = new CreateCollectivityActivity();
        invalid.label = "Activité incohérente";
        invalid.activityType = ActivityType.MEETING;
        invalid.memberOccupationConcerned = List.of(MemberOccupation.JUNIOR);
        invalid.executiveDate = LocalDate.of(2026, 6, 1);
        invalid.recurrenceRule = new MonthlyRecurrenceRule();
        invalid.recurrenceRule.weekOrdinal = 1;
        invalid.recurrenceRule.dayOfWeek = WeekDay.SA;

        var exception = assertThrows(RuntimeException.class,
                () -> apiClient.createCollectivityActivities("col-1", List.of(invalid)));

        log.info("Erreur attendue (executiveDate + recurrenceRule) : " + exception.getMessage());
        assertTrue(exception.getMessage().contains("HTTP Error: 400"),
                "Fournir executiveDate et recurrenceRule simultanément doit retourner 400");
    }

    // ================================================================
    // Helpers
    // ================================================================

    private CreateActivityMemberAttendance attendance(String memberId, AttendanceStatus status) {
        var a = new CreateActivityMemberAttendance();
        a.memberIdentifier = memberId;
        a.attendanceStatus = status;
        return a;
    }

    private void assertStatus(List<ActivityMemberAttendance> list,
                               String memberId, AttendanceStatus expected) {
        var entry = list.stream()
                .filter(a -> a.memberDescription != null && memberId.equals(a.memberDescription.id))
                .findFirst();
        assertTrue(entry.isPresent(), "Aucune entrée de présence pour le membre " + memberId);
        assertEquals(expected, entry.get().attendanceStatus,
                "Statut inattendu pour " + memberId
                        + " : attendu=" + expected
                        + ", obtenu=" + entry.get().attendanceStatus);
    }
}
