package app;

import static java.lang.String.format;
import static org.grapple.query.EntityRootBuilder.entityRoot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.grapple.query.RootFetchSet;
import org.grapple.query.TabularResultList;
import org.grapple.query.TabularResultRow;
import org.grapple.query.impl.QueryProvider;
import sandbox.grapple.CompanyField;
import sandbox.grapple.UserField;
import sandbox.grapple.entity.Company;
import sandbox.grapple.entity.User;

public final class SandboxQueryTests {

    public static void main(String[] args) throws Exception {
        Launch.runTest(SandboxQueryTests::testUnsharedSingularJoin);
        Launch.runTest(SandboxQueryTests::testUnsharedSetJoin);
        Launch.runTest(SandboxQueryTests::testBasicNonQuery);
        Launch.runTest(SandboxQueryTests::testDeferredSelection);
        Launch.runTest(SandboxQueryTests::testDuplicateDeferredSelection);
    }

    private static void testUnsharedSingularJoin(EntityManager entityManager) {
        final RootFetchSet<User> query = QueryProvider.newQuery(User.class);
        query.select(UserField.Id);
        query.join(UserField.CompanyWithId99, tblCompany -> tblCompany.select(CompanyField.ID));
        PrettyPrint(query.execute(entityManager, entityRoot(User.class)).asTabular());
    }

    private static void testUnsharedSetJoin(EntityManager entityManager) {
        final RootFetchSet<Company> query = QueryProvider.newQuery(Company.class);
        query.select(CompanyField.ID);
        query.join(CompanyField.UsersWithId99, tblUser -> tblUser.select(UserField.Id));
        PrettyPrint(query.execute(entityManager, entityRoot(Company.class)).asTabular());
    }

    private static void testBasicNonQuery(EntityManager entityManager) {
        final RootFetchSet<User> query = QueryProvider.newQuery(User.class);
        query.select(UserField.Id);
        query.select(UserField.UserCustomDetails);
        PrettyPrint(query.execute(entityManager, entityRoot(User.class)).asTabular());
    }

    private static void testDeferredSelection(EntityManager entityManager) {
        final RootFetchSet<User> query = QueryProvider.newQuery(User.class);
        query.select(UserField.Id);
        query.join(UserField.Company, company -> {
            company.join(CompanyField.OwnerNullAllowed, tblOwner -> {
                tblOwner.select(UserField.Id);
                tblOwner.select(UserField.UserCustomDetails);
                tblOwner.select(UserField.UserCustomDetails1);
                tblOwner.select(UserField.UserCustomDetails2);
                tblOwner.select(UserField.UserCustomDetails3);
            });
        });
        query.select(UserField.UserCustomDetails);
        query.select(UserField.UserCustomDetails1);
        query.select(UserField.UserCustomDetails2);
        query.select(UserField.UserCustomDetails3);
        PrettyPrint(query.execute(entityManager, entityRoot(User.class)).asTabular());
    }

    private static void testDuplicateDeferredSelection(EntityManager entityManager) {
        final RootFetchSet<User> query = QueryProvider.newQuery(User.class);
        query.select(UserField.Id);
        // Make sure displayName is loaded only once
        query.select(UserField.UserCustomDetails);
        query.select(UserField.UserCustomDetails1);
        query.select(UserField.UserCustomDetails2);
        PrettyPrint(query.execute(entityManager, entityRoot(User.class)).asTabular());
    }

    private static void PrettyPrint(TabularResultList results) {
        if (results.isEmpty()) {
            return;
        }
        final List<Map<String, Object>> allRows = new ArrayList<>();
        for (TabularResultRow result: results) {
            allRows.add(result.getValues());
        }
        final List<String> columnNames = new ArrayList<>(allRows.get(0).keySet());
        final Map<String, Integer> columnWidths = new HashMap<>();
        // Initialise with column widths
        for (String columnName: columnNames) {
            columnWidths.put(columnName, columnName.length());
        }
        // Now loop through all rows ...
        for (Map<String, Object> row: allRows) {
            for (String columnName: columnNames) {
                columnWidths.put(columnName, Math.max(columnWidths.get(columnName), String.valueOf(row.get(columnName)).length()));
            }
        }
        StringBuffer buffer = new StringBuffer();
        // First write header row
        for (String columnName: columnNames) {
            buffer.append(format("| %s ", columnName));
            final int distanceToPad = columnWidths.get(columnName) - columnName.length();
            if (distanceToPad > 0) {
                buffer.append(new String(new char[distanceToPad]).replace("\0", " "));
            }
        }
        buffer.append(format("|%n")); // Final terminating
        for (String columnName: columnNames) {
            buffer.append(format("|%s", new String(new char[columnName.length() + columnWidths.get(columnName) - columnName.length() + 2]).replace("\0", "-")));
        }
        buffer.append(format("|%n")); // Final terminating
        for (Map<String, Object> row: allRows) {
            for (String columnName: columnNames) {
                final String value = String.valueOf(row.get(columnName));
                buffer.append(format("| %s ", value));
                final int distanceToPad = columnWidths.get(columnName) - value.length();
                if (distanceToPad > 0) {
                    buffer.append(new String(new char[distanceToPad]).replace("\0", " "));
                }
            }
            buffer.append(format("|%n"));
        }
        buffer.append(format("%n"));
        System.out.println(buffer);
    }
}
