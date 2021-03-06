package idv.jhuang.sql4j.test;

import static idv.jhuang.sql4j.Entity.entity;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import idv.jhuang.sql4j.Configuration;
import idv.jhuang.sql4j.Configuration.Type;
import idv.jhuang.sql4j.DaoFactory;
import idv.jhuang.sql4j.DaoFactory.Dao;
import idv.jhuang.sql4j.Entity;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class DaoTest {
	private static final Logger log = LogManager.getLogger(DaoTest.class);
	
	private DaoFactory factory;
	
	
	
	@Before
	public void setup() throws Exception {
		factory = new DaoFactory(Configuration.parseConfiguration(
				getClass().getClassLoader().getResourceAsStream("sql4j.xml")));
		factory.init();
	}
	
	
	
	//@Test
	public void testCreateMaster() throws Exception {
		
		try(Dao dao = factory.createDao()) {
		
			Entity student = dao.createOrUpdate("Student", entity(
					"name", "Jack Huang",
					"age", 24,
					"gender", "male",
					"dateOfBirth", "1989-12-17",
					"hasGraduated", true,
					"car", "Nissan Altima",
					"school", entity("name", "UT Austin"),
					"transcript", entity("gpa", 3.95),
					"courses", asList(
							entity("name", "Microarchitecture"),
							entity("name", "Real-Time Operating System")),
					"emails", asList(
							entity("address", "jack.huang78@gmail.com"),
							entity("address", "jack.huang@utexas.edu")),
					"advisor", entity("name", "Sanjay Srinivasan"),
					"dormRoom", entity("location", "Off campus")
					));
			dao.commit();
			
			assertNotNull(student);
			assertNotNull(student.get("id"));
			assertNotNull(student.get("transcript"));
			assertNotNull(student.get("school"));
			assertNotNull(student.get("advisor"));
			assertNotNull(student.get("dormRoom"));
			assertNotNull(student.get("emails"));
			assertEquals(2, student.getList("emails", Entity.class).size());
			assertNotNull(student.getList("emails", Entity.class).get(0).get("id"));
			assertNotNull(student.getList("emails", Entity.class).get(1).get("id"));
			assertNotNull(student.get("courses"));
			assertEquals(2, student.getList("courses", Entity.class).size());
			assertNotNull(student.getList("courses", Entity.class).get(0).get("id"));
			assertNotNull(student.getList("courses", Entity.class).get(1).get("id"));
			
			Object studentId = student.get("id");
			Object transcriptId = student.get("transcript", Entity.class).get("id");
			Object schoolId = student.get("school", Entity.class).get("id");
			Object advisorId = student.get("advisor", Entity.class).get("id");
			Object dormRoomId = student.get("dormRoom", Entity.class).get("id");
			Object email1Id = student.getList("emails", Entity.class).get(0).get("id");
			Object email2Id = student.getList("emails", Entity.class).get(1).get("id");
			Object course1Id = student.getList("courses", Entity.class).get(0).get("id");
			Object course2Id = student.getList("courses", Entity.class).get(1).get("id");
			
			List<Entity> students = dao.read("Student", asList(studentId), entity(
					"id", null,
					"name", null, "age", null, "gender", null, "dateOfBirth", null, "hasGraduated", null, "car", null,
					"school", entity("id", null, "name", null),
					"transcript", entity("id", null, "gpa", null),
					"courses", entity("id", null, "name", null),
					"emails", entity("id", null, "address", null),
					"advisor", entity("id", null, "name", null),
					"dormRoom", entity("id", null, "location", null)
					));
			assertNotNull(students);
			assertEquals(1, students.size());
			
			student = students.get(0);
			assertEquals(studentId, student.get("id"));
			assertEquals("Jack Huang", student.get("name"));
			assertEquals(24, (int) student.get("age"));
			assertEquals("male", student.get("gender"));
			assertEquals(true, student.get("hasGraduated"));
			assertEquals("Nissan Altima", student.get("car"));
			
			
			Entity transcript = student.get("transcript");
			assertNotNull(transcript);
			assertEquals(transcriptId, transcript.get("id"));
			assertEquals(3.95, transcript.get("gpa"), 0.0);
			
			Entity school = student.get("school");
			assertNotNull(school);
			assertEquals(schoolId, school.get("id"));
			assertEquals("UT Austin", school.get("name"));
			
			Entity advisor = student.get("advisor");
			assertNotNull(advisor);
			assertEquals(advisorId, advisor.get("id"));
			assertEquals("Sanjay Srinivasan", advisor.get("name"));
			
			Entity dormRoom = student.get("dormRoom");
			assertNotNull(dormRoom);
			assertEquals(dormRoomId, dormRoom.get("id"));
			assertEquals("Off campus", dormRoom.get("location"));
			
			List<Entity> emails = student.get("emails");
			assertNotNull(emails);
			assertEquals(2, emails.size());
			Entity email1 = emails.get(0);
			Entity email2 = emails.get(1);
			assertNotNull(email1);
			assertNotNull(email2);
			assertEquals(email1Id, email1.get("id"));
			assertEquals(email2Id, email2.get("id"));
			
			List<Entity> courses = student.get("courses");
			assertNotNull(courses);
			assertEquals(2, courses.size());
			Entity course1 = courses.get(0);
			Entity course2 = courses.get(1);
			assertNotNull(course1);
			assertNotNull(course2);
			assertEquals(course1Id, course1.get("id"));
			assertEquals(course2Id, course2.get("id"));
		}
	}
	
	//@Test
	public void testCreateSlave() throws Exception {

		
		try(Dao dao = factory.createDao()) {
			
			
			//====================
			//	OneToOne slave
			//====================			
			Entity transcript = dao.createOrUpdate("Transcript", entity(
					"gpa", 3.99,
					"owner", entity("name", "slave1")));
			dao.commit();
			
			assertNotNull(transcript);
			Object transcriptId = transcript.get("id");
			assertNotNull(transcriptId);
			Entity slave1 = transcript.get("owner");
			assertNotNull(slave1);
			Object slave1Id = slave1.get("id");
			assertNotNull(slave1Id);
			
			
			List<Entity> transcripts = dao.read("Transcript", asList(transcriptId), entity(
					"id", null, "gpa", null, "owner", entity("id", null, "name", null)
					));
			dao.commit();
			assertNotNull(transcripts);
			transcript = transcripts.get(0);
			assertNotNull(transcript);
			assertEquals(transcriptId, transcript.get("id"));
			assertEquals(3.99, transcript.get("gpa"), 0.0);
			slave1 = transcript.get("owner");
			assertNotNull(slave1);
			assertEquals(slave1Id, slave1.get("id"));
			assertEquals("slave1", slave1.get("name"));
			
			//====================
			//	OneToOne slave sparse
			//====================	
			
			Entity dormRoom = dao.createOrUpdate("DormRoom", entity(
					"location", "Jester 9F",
					"student", entity("name", "slave2")));
			assertNotNull(dormRoom);
			Object dormRoomId = dormRoom.get("id");
			assertNotNull(dormRoomId);
			Entity slave2 = dormRoom.get("student");
			assertNotNull(slave2);
			Object slave2Id = slave2.get("id");
			assertNotNull(slave2Id);
			
			List<Entity> dormRooms = dao.read("DormRoom", asList(dormRoomId), entity(
					"id", null, "location", null, "student", entity("id", null, "name", null)
					));
			dao.commit();
			assertNotNull(dormRooms);
			dormRoom = dormRooms.get(0);
			assertNotNull(dormRoom);
			assertEquals(dormRoomId, dormRoom.get("id"));
			assertEquals("Jester 9F", dormRoom.get("location"));
			slave2 = dormRoom.get("student");
			assertNotNull(slave2);
			assertEquals(slave2Id, slave2.get("id"));
			assertEquals("slave2", slave2.get("name"));
			
			//====================
			//	OneToMany slave sparse
			//====================
			
			Entity advisor = dao.createOrUpdate("Advisor", entity(
					"name", "Patt",
					"students", asList(
							entity("name", "slave3"), 
							entity("name", "slave4"))));
			dao.commit();
			assertNotNull(advisor);
			Object advisorId = advisor.get("id");
			assertNotNull(advisorId);
			List<Entity> slaves34 = advisor.get("students");
			assertNotNull(slaves34);
			assertEquals(2, slaves34.size());
			Object slave3Id = slaves34.get(0).get("id");
			Object slave4Id = slaves34.get(1).get("id");
			assertNotNull(slave3Id);
			assertNotNull(slave4Id);
			
			List<Entity> advisors = dao.read("Advisor", asList(advisorId), entity(
					"id", null, "name", null, "students", entity("id", null, "name", null)
					));
			assertNotNull(advisors);
			advisor = advisors.get(0);
			assertNotNull(advisor);
			assertEquals(advisorId, advisor.get("id"));
			assertEquals("Patt", advisor.get("name"));
			slaves34 = advisor.get("students");
			assertNotNull(slaves34);
			assertEquals(2, slaves34.size());
			assertEquals(slave3Id, slaves34.get(0).get("id"));
			assertEquals("slave3", slaves34.get(0).get("name"));
			assertEquals(slave4Id, slaves34.get(1).get("id"));
			assertEquals("slave4", slaves34.get(1).get("name"));
			
			//====================
			//	OneToMany slave
			//====================
			
			Entity school = dao.createOrUpdate("School", entity( 
					"name", "Stanford",
					"students", asList(
							entity("name", "slave5"), 
							entity("name", "slave6"))
					));
			dao.commit();
			assertNotNull(school);
			Object schoolId = school.get("id");
			assertNotNull(schoolId);
			List<Entity> slaves56 = school.get("students");
			assertNotNull(slaves56);
			assertEquals(2, slaves34.size());
			Object slave5Id = slaves56.get(0).get("id");
			Object slave6Id = slaves56.get(1).get("id");
			assertNotNull(slave5Id);
			assertNotNull(slave6Id);
			
			List<Entity> schools = dao.read("School", asList(schoolId), entity(
					"id", null, "name", null, "students", entity("id", null, "name", null)
					));
			assertNotNull(schools);
			school = schools.get(0);
			assertNotNull(school);
			assertEquals(schoolId, school.get("id"));
			assertEquals("Stanford", school.get("name"));
			slaves56 = school.get("students");
			assertNotNull(slaves56);
			assertEquals(2, slaves56.size());
			assertEquals(slave5Id, slaves56.get(0).get("id"));
			assertEquals("slave5", slaves56.get(0).get("name"));
			assertEquals(slave6Id, slaves56.get(1).get("id"));
			assertEquals("slave6", slaves56.get(1).get("name"));
			
			//====================
			//	ManyToMany slave
			//====================
			Entity course = dao.createOrUpdate("Course", entity(
					"name", "Machine Learning",
					"students", asList(
							entity("name", "slave7"), 
							entity("name", "slave8"))
					));
			dao.commit();
			assertNotNull(course);
			Object courseId = course.get("id");
			assertNotNull(courseId);
			List<Entity> slaves78 = course.get("students");
			assertNotNull(slaves78);
			assertEquals(2, slaves78.size());
			Object slave7Id = slaves78.get(0).get("id");
			Object slave8Id = slaves78.get(1).get("id");
			assertNotNull(slave7Id);
			assertNotNull(slave8Id);
			
			List<Entity> courses = dao.read("Course", asList(courseId), entity(
					"id", null, "name", null, "students", entity("id", null, "name", null)
					));
			assertNotNull(courses);
			course = courses.get(0);
			assertNotNull(course);
			assertEquals(courseId, course.get("id"));
			assertEquals("Machine Learning", course.get("name"));
			slaves78 = course.get("students");
			assertNotNull(slaves78);
			assertEquals(2, slaves78.size());
			assertEquals(slave7Id, slaves78.get(0).get("id"));
			assertEquals("slave7", slaves78.get(0).get("name"));
			assertEquals(slave8Id, slaves78.get(1).get("id"));
			assertEquals("slave8", slaves78.get(1).get("name"));
			
			//====================
			//	ManyToOne slave
			//====================
			Entity email = dao.createOrUpdate("Email", entity(
					"address", "jack.huang@stanford.edu",
					"owner", entity("name", "slave9")
					));
			dao.commit();
			assertNotNull(email);
			Object emailId = email.get("id");
			assertNotNull(emailId);
			Entity slave9 = email.get("owner");
			assertNotNull(slave9);
			Object slave9Id = slave9.get("id");
			assertNotNull(slave9Id);
			
			List<Entity> emails = dao.read("Email", asList(emailId), entity(
					"id", null, "address", null, "owner", entity("id", null, "name", null)
					));
			dao.commit();
			assertNotNull(emails);
			email = emails.get(0);
			assertNotNull(email);
			assertEquals(emailId, email.get("id"));
			assertEquals("jack.huang@stanford.edu", email.get("address"));
			slave9 = email.get("owner");
			assertNotNull(slave9);
			assertEquals(slave9Id, slave9.get("id"));
			assertEquals("slave9", slave9.get("name"));
			
		}	
		
		
	}
	
	//@Test
	public void testUpdateMaster() throws SQLException {
		
		try(Dao dao = factory.createDao()) {
			Entity student1 = dao.createOrUpdate("Student", 
					entity().set("name", "Jack Huang"));
			Entity transcript = dao.createOrUpdate("Transcript", 
					entity().set("gpa", 3.95));
			Entity school = dao.createOrUpdate("School", 
					entity().set("name", "UT Austin"));
			Entity advisor = dao.createOrUpdate("Advisor", 
					entity().set("name", "Sanjay Srinivasan"));
			Entity dormRoom = dao.createOrUpdate("DormRoom", 
					entity().set("location", "Off campus"));
			List<Entity> emails = dao.createOrUpdate("Email", asList(
					entity().set("address", "jack.huang78@gmail.com"),
					entity().set("address", "jack.huang@utexas.edu")));
			List<Entity> courses = dao.createOrUpdate("Course", asList(
					entity().set("name", "Microarchitecture"),
					entity().set("name", "RTOS")));
			
			Object student1Id = student1.get("id");
			Object transcriptId = transcript.get("id");
			Object schoolId = school.get("id");
			Object advisorId = advisor.get("id");
			Object dormRoomId = dormRoom.get("id");
			Object email1Id = emails.get(0).get("id");
			Object email2Id = emails.get(1).get("id");
			Object course1Id = courses.get(0).get("id");
			Object course2Id = courses.get(1).get("id");
			
			student1.set("transcript", transcript)
				.set("school", school)
				.set("advisor", advisor)
				.set("dormRoom", dormRoom)
				.set("emails", emails)
				.set("courses", courses);
			dao.createOrUpdate("Student", student1);
			dao.commit();
			
			student1 = dao.read("Student", student1Id, entity()
					.put("id").put("name")
					.set("transcript", entity().put("id").put("gpa"))
					.set("school", entity().put("id").put("name"))
					.set("advisor", entity().put("id").put("name"))
					.set("dormRoom", entity().put("id").put("location"))
					.set("emails", entity().put("id").put("address"))
					.set("courses", entity().put("id").put("name"))
					);
			assertNotNull(student1);
			assertEquals(student1Id, student1.get("id"));
			assertEquals("Jack Huang", student1.get("name"));
			transcript = student1.get("transcript");
			assertNotNull(transcript);
			assertEquals(transcriptId, transcript.get("id"));
			assertEquals(3.95, transcript.get("gpa"), 0.0);
			school = student1.get("school");
			assertNotNull(school);
			assertEquals(schoolId, school.get("id"));
			assertEquals("UT Austin", school.get("name"));
			advisor = student1.get("advisor");
			assertNotNull(advisor);
			assertEquals(advisorId, advisor.get("id"));
			assertEquals("Sanjay Srinivasan", advisor.get("name"));
			dormRoom = student1.get("dormRoom");
			assertNotNull(dormRoom);
			assertEquals(dormRoomId, dormRoom.get("id"));
			assertEquals("Off campus", dormRoom.get("location"));
			emails = student1.get("emails");
			assertNotNull(emails);
			assertEquals(2, emails.size());
			assertEquals(email1Id, emails.get(0).get("id"));
			assertEquals(email2Id, emails.get(1).get("id"));
			assertEquals("jack.huang78@gmail.com", emails.get(0).get("address"));
			assertEquals("jack.huang@utexas.edu", emails.get(1).get("address"));
			courses = student1.get("courses");
			assertNotNull(courses);
			assertEquals(2, courses.size());
			assertEquals(course1Id, courses.get(0).get("id"));
			assertEquals(course2Id, courses.get(1).get("id"));
			assertEquals("Microarchitecture", courses.get(0).get("name"));
			assertEquals("RTOS", courses.get(1).get("name"));
			
			log.info("========== Student2 ==========");
			
			
			Entity student2 =  dao.createOrUpdate("Student", 
					entity("name", "Derek Chen"));
			Object student2Id = student2.get("id");
			dao.commit();
			student2.set("transcript", transcript.retain("id"))
				.set("school", school.retain("id"))
				.set("advisor", advisor.retain("id"))
				.set("dormRoom", dormRoom.retain("id"))
				.set("emails", emails.parallelStream().map(
						(Entity e) -> e.retain("id")).collect(Collectors.toList()))
				.set("courses", courses.parallelStream().map(
						(Entity e) -> e.retain("id")).collect(Collectors.toList()));
			dao.createOrUpdate("Student", student2);
			dao.commit();
			
			student1 = dao.read("Student", student1Id, entity()
					.put("id").put("name")
					.set("transcript", entity().put("id").put("gpa"))
					.set("school", entity().put("id").put("name"))
					.set("advisor", entity().put("id").put("name"))
					.set("dormRoom", entity().put("id").put("location"))
					.set("emails", entity().put("id").put("address"))
					.set("courses", entity().put("id").put("name"))
					);
			transcript = student1.get("transcript");
			assertNull(transcript);
			dormRoom = student1.get("dormRoom");
			assertNull(dormRoom);
			school = student1.get("school");
			assertNotNull(school);
			assertEquals(schoolId, school.get("id"));
			assertEquals("UT Austin", school.get("name"));
			advisor = student1.get("advisor");
			assertNotNull(advisor);
			assertEquals(advisorId, advisor.get("id"));
			assertEquals("Sanjay Srinivasan", advisor.get("name"));
			emails = student1.get("emails");
			assertNotNull(emails);
			assertEquals(0, emails.size());
			courses = student1.get("courses");
			assertNotNull(courses);
			assertEquals(2, courses.size());
			assertEquals(course1Id, courses.get(0).get("id"));
			assertEquals(course2Id, courses.get(1).get("id"));
			assertEquals("Microarchitecture", courses.get(0).get("name"));
			assertEquals("RTOS", courses.get(1).get("name"));
			
			student2 = dao.read("Student", student2Id, entity()
					.put("id").put("name")
					.set("transcript", entity().put("id").put("gpa"))
					.set("school", entity().put("id").put("name"))
					.set("advisor", entity().put("id").put("name"))
					.set("dormRoom", entity().put("id").put("location"))
					.set("emails", entity().put("id").put("address"))
					.set("courses", entity().put("id").put("name"))
					);
			transcript = student2.get("transcript");
			assertNotNull(transcript);
			assertEquals(transcriptId, transcript.get("id"));
			dormRoom = student2.get("dormRoom");
			assertNotNull(dormRoom);
			assertEquals(dormRoomId, dormRoom.get("id"));
			assertEquals("Off campus", dormRoom.get("location"));
			school = student2.get("school");
			assertNotNull(school);
			assertEquals(schoolId, school.get("id"));
			assertEquals("UT Austin", school.get("name"));
			advisor = student2.get("advisor");
			assertNotNull(advisor);
			assertEquals(advisorId, advisor.get("id"));
			assertEquals("Sanjay Srinivasan", advisor.get("name"));
			emails = student2.get("emails");
			assertNotNull(emails);
			assertEquals(2, emails.size());
			assertEquals(email1Id, emails.get(0).get("id"));
			assertEquals(email2Id, emails.get(1).get("id"));
			assertEquals("jack.huang78@gmail.com", emails.get(0).get("address"));
			assertEquals("jack.huang@utexas.edu", emails.get(1).get("address"));
			courses = student2.get("courses");
			assertNotNull(courses);
			assertEquals(2, courses.size());
			assertEquals(course1Id, courses.get(0).get("id"));
			assertEquals(course2Id, courses.get(1).get("id"));
			assertEquals("Microarchitecture", courses.get(0).get("name"));
			assertEquals("RTOS", courses.get(1).get("name"));
			
			
		}

	}
	
	@Test
	public void testUpdateSlave() throws SQLException {
		try(Dao dao = factory.createDao()) {
			Entity student1 = dao.createOrUpdate("Student", 
					entity().set("name", "Jack Huang"));
			Entity transcript = dao.createOrUpdate("Transcript", 
					entity().set("gpa", 3.95));
			Entity school = dao.createOrUpdate("School", 
					entity().set("name", "UT Austin"));
			Entity advisor = dao.createOrUpdate("Advisor", 
					entity().set("name", "Sanjay Srinivasan"));
			Entity dormRoom = dao.createOrUpdate("DormRoom", 
					entity().set("location", "Off campus"));
			List<Entity> emails = dao.createOrUpdate("Email", asList(
					entity().set("address", "jack.huang78@gmail.com"),
					entity().set("address", "jack.huang@utexas.edu")));
			List<Entity> courses = dao.createOrUpdate("Course", asList(
					entity().set("name", "Microarchitecture"),
					entity().set("name", "RTOS")));
			
			Object student1Id = student1.get("id");
			Object transcriptId = transcript.get("id");
			Object schoolId = school.get("id");
			Object advisorId = advisor.get("id");
			Object dormRoomId = dormRoom.get("id");
			Object email1Id = emails.get(0).get("id");
			Object email2Id = emails.get(1).get("id");
			Object course1Id = courses.get(0).get("id");
			Object course2Id = courses.get(1).get("id");
			
			transcript.set("owner", student1);
			dao.createOrUpdate("Transcription", transcript);
			dao.commit();
			
			
			
			
			
			
			Entity student2 = dao.createOrUpdate("Student", entity().set("name", "Derek Chen"));
		}
	}
	
	
	
	

}
