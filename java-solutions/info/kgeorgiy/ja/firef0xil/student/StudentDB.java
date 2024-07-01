package info.kgeorgiy.ja.firef0xil.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StudentDB implements StudentQuery, GroupQuery, AdvancedQuery {

    private static final Comparator<Student> FULL_NAME_COMPARATOR =
            Comparator.comparing(Student::getLastName)
                    .thenComparing(Student::getFirstName)
                    .reversed()
                    .thenComparing(Student::getId);

    private static final Function<Student, String> FULL_NAME_FUNCTION =
            student -> student.getFirstName() + " " + student.getLastName();

    private static final Collector<Student, ?, Map<GroupName, List<Student>>> STUDENT_MAP_COLLECTOR =
            Collectors.groupingBy(Student::getGroup);

    private static final Comparator<Student> STUDENT_ID_COMPARATOR = Comparator.comparing(Student::getId);

    private List<Group> groupsListGetter(Collection<Student> students,
                                         Comparator<Student> comparator) {
        return sort(students, comparator)
                .stream()
                .collect(STUDENT_MAP_COLLECTOR)
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new Group(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return groupsListGetter(students, FULL_NAME_COMPARATOR);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return groupsListGetter(students, Student::compareTo);
    }

    private <T, R> R groupsGetter(Collection<Student> students,
                                 Collector<Student, ?, Map<R, T>> collector,
                                 Comparator<Map.Entry<R, T>> comparator,
                                 R defaultVal) {
        return students
                .stream()
                .collect(collector)
                .entrySet()
                .stream()
                .max(comparator)
                .map(Map.Entry::getKey)
                .orElse(defaultVal);
    }

    @Override
    public String getMostPopularName(Collection<Student> students) {
        return groupsGetter(students,
                Collectors.groupingBy(Student::getFirstName, Collectors.mapping(Student::getGroup, Collectors.toSet())),
                Comparator
                        .comparingInt((Map.Entry<String, Set<GroupName>> g) -> g.getValue().size())
                        .thenComparing(Map.Entry::getKey),
                "");
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return groupsGetter(students,
                Collectors.groupingBy(Student::getGroup, Collectors.counting()),
                Map.Entry.<GroupName, Long>comparingByValue().thenComparing(Map.Entry::getKey),
                null);
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return groupsGetter(students,
                STUDENT_MAP_COLLECTOR,
                Comparator
                        .comparingInt((Map.Entry<GroupName, List<Student>> g) -> getDistinctFirstNames(g.getValue()).size())
                        .thenComparing(Map.Entry::getKey, Collections.reverseOrder(GroupName::compareTo)),
                null);
    }

    private <T, R> R getByMapper(List<Student> students, Function<Student, T> mapper, Collector<T, ?, R> collector) {
        return students
                .stream()
                .map(mapper)
                .collect(collector);
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getByMapper(students, Student::getFirstName, Collectors.toList());
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getByMapper(students, Student::getLastName, Collectors.toList());
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getByMapper(students, Student::getGroup, Collectors.toList());
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getByMapper(students, FULL_NAME_FUNCTION, Collectors.toList());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getByMapper(sort(students, Comparator.comparing(Student::getFirstName)),
                Student::getFirstName,
                Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students
                .stream()
                .max(STUDENT_ID_COMPARATOR)
                .map(Student::getFirstName)
                .orElse("");
    }

    private List<Student> sort(Collection<Student> students, Comparator<Student> comparator){
        return students
                .stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sort(students, STUDENT_ID_COMPARATOR);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sort(students, FULL_NAME_COMPARATOR);
    }

    private List<Student> find(Collection<Student> students,
                               Predicate<Student> predicate) {
        return sortStudentsByName(students
                .stream()
                .filter(predicate)
                .collect(Collectors.toList()));
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return find(students, student -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return find(students, student -> student.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return find(students, student -> student.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return findStudentsByGroup(students, group)
                .stream()
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, (o, n) -> n.compareTo(o) < 0 ? n : o));
    }

    private <T> List<T> getByIndices(Collection<Student> students,
                                     int[] indices,
                                     Function<Student, T> mapper) {
        return Arrays.stream(indices)
                .mapToObj(List.copyOf(students)::get)
                .map(mapper)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(Collection<Student> students, int[] indices) {
        return getByIndices(students, indices, FULL_NAME_FUNCTION);
    }
}
