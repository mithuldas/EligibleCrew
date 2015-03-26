/*
 * EligibleCrew application blueprint
 * - Mithul Mangaldas
 *
 */

- QueryBuilder.java: generates the final SQL code.

String getBaseRankFleetComponent();
String getQualComponent(); // may make use of another class that determines the 
required qualifications

String getNonLeaveDaysComponent();
String mergeComponents();

String query = QueryBuilder('330', 'ALL', 'April', '2015', '5'); // FD
String query = QueryBuilder('330', 'BCA', 'April', '2015', '5'); // CC 
String query = QueryBuilder('330', 'BCA', 'April', '2015', '5', 'U'); // CC + upranking


- EligibleCrewDAO.java - runs the query against the DB and fetches resultset