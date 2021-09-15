package org.racetrack.hopper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

import static java.lang.Math.abs;
import static java.lang.Math.addExact;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.util.Objects.isNull;

/**
 * Assumptions: <br/>
 *
 * Line 3: first two coordinates are the start point and last two coordinates are the end point to reach. <br/>
 * "P" Lines are in format: x1 x2 y1 y2 so there coordinates will be re-arranges to (x1,y1) and (x2,y2) <br/>
 * Identifying the available hops at every point and finding the shortest distance to end point considering only empty areas <br/>
 * If no empty area is found then no solution returns. <br/>
 */
public class Main
{

    public static void main(String[] args) throws Exception
    {
        // write your code here
        // below to accelerate/slow down the speeds
        List<Point> speedDeterminer = new ArrayList<>();
        speedDeterminer.add(new Point(1, -1));
        speedDeterminer.add(new Point(-1, 1));
        speedDeterminer.add(new Point(1, 1));
        speedDeterminer.add(new Point(1, 0));
        speedDeterminer.add(new Point(0, 1));
        speedDeterminer.add(new Point(-1, 0));
        speedDeterminer.add(new Point(0, -1));
        speedDeterminer.add(new Point(-1, -1));

        Scanner input = new Scanner(System.in);
        System.out.println("Enter number of Test cases");
        int numberOfTestCases = Integer.parseInt(input.nextLine());
        int countTestCase = 1;
        while (countTestCase <= numberOfTestCases)
        {
            System.out.println(" TestCase: " + countTestCase);
            System.out.println(" Enter the grid size: x y (1 blank between them): ");
            String line1 = input.nextLine();
            String[] splitLine1 = line1.split(" ");
            if (splitLine1.length != 2)
            {
                throw new Exception("Invalid input. 2 integers required with a blank in between");
            }
            int gridXSize, gridYSize;
            try
            {
                gridXSize = Integer.parseInt(splitLine1[0]);
                gridYSize = Integer.parseInt(splitLine1[1]);

                if (gridXSize > 30 || gridXSize < 0 || gridYSize > 30 || gridYSize < 0)
                {
                    throw new Exception("Invalid Line1 input: wrong grid size details entered. Maximum is 30X30");
                }
                System.out.println("Grid size: " + gridXSize + ", " + gridYSize);
            }
            catch (NumberFormatException nfe)
            {
                throw new NumberFormatException("Not an integer for line 1");
            }

            System.out.println("Enter the start and end point");
            String line2 = input.nextLine();
            String[] splitLine2 = line2.split(" ");
            if (splitLine2.length != 4)
            {
                throw new Exception("Invalid Line2 input. 4 integers required with a blank in between all integers");
            }

            int coordStartX1 = Integer.parseInt(splitLine2[0]);
            int coordStartY1 = Integer.parseInt(splitLine2[1]);
            Point startPoint = new Point(coordStartX1, coordStartY1);

            int coordEndX2 = Integer.parseInt(splitLine2[2]);
            int coordEndY2 = Integer.parseInt(splitLine2[3]);
            Point endPoint = new Point(coordEndX2, coordEndY2);

            if (coordStartX1 > gridXSize || coordEndX2 > gridXSize || coordStartY1 > gridYSize || coordEndY2 > gridYSize)
            {
                throw new Exception("coordinate size cannot be greater than grid size ");
            }
            System.out.println("Start coordinates : " + coordStartX1 + ", " + coordStartY1);
            System.out.println("End coordinates : " + coordEndX2 + ", " + coordEndY2);

            System.out.println("Enter number of obstacles");
            int numberOfObstacles = Integer.parseInt(input.nextLine());
            System.out.println("choosen: " + numberOfObstacles);
            int countObstacles = 1;
            int[] x1y1 = new int[2];
            int[] x2y2 = new int[2];
            Map<Integer, List<Point>> obstacleListMap = new HashMap<>();
            while (countObstacles <= numberOfObstacles)
            {
                System.out.println("Enter x1, x2, y1, y2 of obstacles start and end range");
                String obstacle = input.nextLine();
                String[] obstacleCoords = obstacle.split(" ");
                if (obstacleCoords.length != 4)
                {
                    throw new Exception("Line " + (countObstacles + 3) + " is invalid. It needs 4 coordinates");
                }
                x1y1[0] = Integer.parseInt(obstacleCoords[0]);
                x1y1[1] = Integer.parseInt(obstacleCoords[2]);

                x2y2[0] = Integer.parseInt(obstacleCoords[1]);
                x2y2[1] = Integer.parseInt(obstacleCoords[3]);

                if (x1y1[0] > x2y2[0] || x1y1[1] > x2y2[1])
                {
                    throw new Exception("Not allowed x1>x2 OR y1>y2");
                }

                Point obstacle1 = new Point(x1y1[0], x1y1[1]);
                Point obstacle2 = new Point(x2y2[0], x2y2[1]);

                List<Point> obstacleList = new ArrayList<>();
                obstacleList.add(obstacle1);
                obstacleList.add(obstacle2);
                obstacleListMap.put(countObstacles, obstacleList);

                System.out.println("Start obstacle coordinates : " + obstacle1.getX() + ", " + obstacle1.getY());
                System.out.println("End coordinates : " + obstacle2.getX() + ", " + obstacle2.getY());
                countObstacles++;
            }

            List<Point> obstaclePoints = new ArrayList<>(determineObstaclePoints(obstacleListMap));
            int numberOfHops = findPossibleHops(startPoint, endPoint, obstaclePoints, speedDeterminer);
            if (numberOfHops == 0)
            {
                System.out.println("No solution");
                break;
            }
            else
            {
                System.out.println("Optimal solution takes " + numberOfHops + " hops");
            }
            countTestCase++;
        }


    }


    private static int findPossibleHops(Point startPoint, Point endPoint, List<Point> obstacles, List<Point> speedDeterminer)
    {
        List<Point> possibleHopPoints = findPossibleHopPoints(startPoint, endPoint, speedDeterminer);
        List<Point> availableHopPoints = filterHopPointsBasedOnOccupied(possibleHopPoints, obstacles);
        Point currentPoint = startPoint;
        int hopCount = 0;
        while (currentPoint != endPoint || availableHopPoints.size() != 0)
        {
            double closestDistance = Double.MAX_VALUE;
            currentPoint = determinePointClosestDistanceToEndPoint(availableHopPoints, endPoint, closestDistance);
            if (isNull(currentPoint))
            {
                return 0;
            }
            hopCount += 1;
            if (currentPoint.equals(endPoint))
            {
                return hopCount;
            }
            possibleHopPoints = findPossibleHopPoints(currentPoint, endPoint, speedDeterminer);
            availableHopPoints = filterHopPointsBasedOnOccupied(possibleHopPoints, obstacles);
        }
        return hopCount;
    }


    private static Point determinePointClosestDistanceToEndPoint(List<Point> availableHopPoints, Point endPoint, double closestDistance)
    {
        Point closestPoint = null;
        for (Point availablePoint : availableHopPoints)
        {
            double distance = determineClosestDistance(availablePoint, endPoint);
            if (distance < closestDistance)
            {
                closestDistance = distance;
                closestPoint = availablePoint;
            }
        }
        availableHopPoints.clear();
        System.out.println("Closest hop point is: " + closestPoint);

        return closestPoint;
    }


    private static double determineClosestDistance(Point current, Point endPoint)
    {
        return sqrt(pow(abs(current.getX() - endPoint.getX()), 2) + pow(abs(current.getY() - endPoint.getY()), 2));
    }


    private static List<Point> filterHopPointsBasedOnOccupied(List<Point> hopPoints, List<Point> obstacles)
    {
        hopPoints.removeAll(obstacles);
        return hopPoints;
    }


    private static List<Point> findPossibleHopPoints(Point startOrCurrent, Point end, List<Point> speedDeterminer)
    {
        List<Point> possibleHopPoints = new ArrayList<>();
        for (Point speed : speedDeterminer)
        {
            int pointX = addExact(startOrCurrent.getX(), speed.getX());
            int pointY = addExact(startOrCurrent.getY(), speed.getY());
            if (pointX < 0 || pointY < 0)
            {
                continue;
            }
            possibleHopPoints.add(new Point(pointX, pointY));
        }
        System.out.println("Possible hop points are: " + possibleHopPoints);
        return possibleHopPoints;
    }


    private static Set<Point> determineObstaclePoints(Map<Integer, List<Point>> obstaclePointsMap)
    {
        Set<Point> obstaclePoints = new HashSet<>();
        for (Integer line : obstaclePointsMap.keySet())
        {
            Set<Point> obstaclePointsLine = determineAllObstaclePointsBetweenTwo(obstaclePointsMap.get(line).get(0), obstaclePointsMap.get(line).get(1), obstaclePoints);
            obstaclePoints.addAll(obstaclePointsLine);
        }
        System.out.println("Obstacle points are: " + obstaclePoints);
        return obstaclePoints;
    }


    private static Set<Point> determineAllObstaclePointsBetweenTwo(Point obstacle1, Point obstacle2, Set<Point> obstaclePoints)
    {

        for (int i = obstacle1.getX(); i <= obstacle2.getX(); i++)
        {
            for (int j = obstacle1.getY(); j <= obstacle2.getY(); j++)
            {
                obstaclePoints.add(new Point(i, j));
            }
        }

        return obstaclePoints;
    }


    static class Point
    {
        private int x;
        private int y;


        Point(int x, int y)
        {
            this.x = x;
            this.y = y;
        }


        public int getX()
        {
            return x;
        }


        public void setX(int x)
        {
            this.x = x;
        }


        public int getY()
        {
            return y;
        }


        public void setY(int y)
        {
            this.y = y;
        }


        @Override
        public int hashCode()
        {
            return Objects.hash(getX(), getY());
        }


        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            Point point = (Point) o;
            return getX() == point.getX() && getY() == point.getY();
        }


        @Override
        public String toString()
        {
            return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
        }
    }

}
