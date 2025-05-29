Project: 
  Customer Support Ticketing System

Summary:
  This backend application serves as a ticketing system for customer support operations. It enables customers to raise support tickets, agents to manage and resolve them, and admins to oversee performance. It includes role-based access, ticket assignment, escalation, and scheduled reminders for unresolved tickets.
 
Key Features:
	• Role-based access (CUSTOMER, AGENT, ADMIN)
	• Secure login using JWT and refresh tokens
	• Ticket creation, assignment, and resolution
	• Commenting system on tickets
	• Analytics and performance tracking for admins
	• JUnit
	• Jacaco (Code coverage)
 
Core APIs:
	1. POST /api/auth/login – Authenticate user
	2. POST /api/auth/refresh-token – Refresh token
	3. POST /api/tickets – Create a new support ticket
	4. GET /api/tickets/mine – Get tickets for the current user
	5. GET /api/tickets/open – List of open tickets for agents
	6. PUT /api/tickets/{id}/assign – Assign ticket to an agent
	7. PUT /api/tickets/{id}/resolve – Mark ticket as resolved
	8. POST /api/tickets/{id}/comment – Add comment to ticket
	9. GET /api/agents – List all support agents
	10. GET /api/tickets/summary – Ticket statistics
	11. GET /api/users/{id} – Fetch user profile
 
Scheduled Jobs:
	• Hourly check for tickets older than 24 hours without resolution
	• Daily summary email to admins for pending escalations
 
Unit Testing & Jacaco:
	• Write the unit testing with mockito for all the API
 	• Ensure the code coverage should be above 90%
