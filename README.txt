/*
 * EligibleCrew application blueprint
 * - Mithul Mangaldas
 *
 */

- QueryBuilder.java: generates the final SQL code.

multi rank queries (CPT+FO and upranking CC combos) to return 2 BRF and Qual strings.
Map getBaseRankFleetComponent();
Map getQualComponent(); // may make use of another class that determines the required qualifications
String getNonLeaveDaysComponent();
String mergeComponents();
List finalizeRanks();

String query = QueryBuilder('ALL', '330', 'April', '2015', '5'); // FD
String query = QueryBuilder('BCA', '330', 'April', '2015', '5'); // CC 
String query = QueryBuilder('BCA', '330','April', '2015', '5', 'U'); // CC + upranking


- EligibleCrewDAO.java - runs the query against the DB and fetches resultset