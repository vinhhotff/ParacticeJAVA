-- ====================================================================
-- 1. Employee Utilization Report
-- Formula: totalAllocation = SUM(allocation_percent) GROUP BY employee_id
-- Sort: totalAllocation DESC
-- ====================================================================
SELECT
    e.employee_id,
    e.employee_code,
    e.full_name,
    COALESCE(SUM(a.allocation_percent), 0) AS total_allocation
FROM employee e
LEFT JOIN allocation a ON e.employee_id = a.employee_id
GROUP BY e.employee_id, e.employee_code, e.full_name
ORDER BY total_allocation DESC;

-- ====================================================================
-- 2. Available Resources Report
-- Formula: available = 100 - SUM(allocation_percent) GROUP BY employee_id
-- Condition: available > 0
-- Sort: available DESC
-- ====================================================================
SELECT
    e.employee_id,
    e.employee_code,
    e.full_name,
    100 - COALESCE(SUM(a.allocation_percent), 0) AS available_percent
FROM employee e
LEFT JOIN allocation a ON e.employee_id = a.employee_id
GROUP BY e.employee_id, e.employee_code, e.full_name
HAVING 100 - COALESCE(SUM(a.allocation_percent), 0) > 0
ORDER BY available_percent DESC;

-- ====================================================================
-- 3. Overloaded Employees Report
-- Formula: totalAllocation = SUM(allocation_percent) GROUP BY employee_id
-- Condition: totalAllocation > 90
-- Sort: totalAllocation DESC
-- ====================================================================
SELECT
    e.employee_id,
    e.employee_code,
    e.full_name,
    COALESCE(SUM(a.allocation_percent), 0) AS total_allocation
FROM employee e
LEFT JOIN allocation a ON e.employee_id = a.employee_id
GROUP BY e.employee_id, e.employee_code, e.full_name
HAVING COALESCE(SUM(a.allocation_percent), 0) > 90
ORDER BY total_allocation DESC;
