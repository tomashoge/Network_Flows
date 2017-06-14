/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package combopt;
//package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Integer.min;
import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 * @author Tomas
 */
public class Main {

    static int[][] capacityArray;
    static int[][] currentArray;

    public static void main(String[] args) throws FileNotFoundException, IOException {

        BufferedReader br = new BufferedReader(new FileReader(args[0]));
        String values[];
        values = br.readLine().trim().split(" ");
        int nCustomers = Integer.parseInt(values[0]);
        int nProducts = Integer.parseInt(values[1]);
        int nCusAndPro = nCustomers + nProducts;
        capacityArray = new int[nCusAndPro + 4][nCusAndPro + 4];
        int newStartArrayIndex = nCusAndPro + 2;
        int newTargetArrayIndex = nCusAndPro + 3;
        int lowerBound, upperBound;
        /**
         * ctu lower a upperbound zakazniku: ukladam si kapacity na 0. radek na
         * pozici T si ukldam soucet minim lowerboundy si ukladam na S sloupec
         * vztahy si ukladam do matice zakaznik\produkt
         */
        for (int i = 1; i < nCustomers + 1; i++) {
            values = br.readLine().trim().split(" ");
            lowerBound = Integer.parseInt(values[0]);
            upperBound = Integer.parseInt(values[1]);
            capacityArray[0][i] = upperBound - lowerBound;
            capacityArray[0][newTargetArrayIndex] += lowerBound;//soucet minim 
            capacityArray[newStartArrayIndex][i] = lowerBound;
            for (int j = 2; j < values.length; j++) {
                capacityArray[i][nCustomers + Integer.parseInt(values[j])] = 1;
            }
        }
        /**
         * do sloupce T ukladam produkt lowerbounds do radku S si ulozim soucet
         * lowerboundu
         *
         */
        values = br.readLine().trim().split(" ");
        for (int i = 0; i < nProducts; i++) {
            capacityArray[1 + nCustomers + i][nCusAndPro + 1] = Integer.MAX_VALUE;
            capacityArray[1 + nCustomers + i][newTargetArrayIndex] = Integer.parseInt(values[i]);
            capacityArray[newStartArrayIndex][nCusAndPro + 1] += Integer.parseInt(values[i]);
        }
        capacityArray[nCusAndPro + 1][0] = Integer.MAX_VALUE;

        currentArray = new int[nCusAndPro + 4][nCusAndPro + 4];

        //1. ford fulkerson
        fordFulkersonAlgorithm(newStartArrayIndex, newTargetArrayIndex, nCusAndPro + 4, nCustomers);

        //overeni splnitelnosti
        boolean isFeasible = true;
        for (int i = 1; i <= nCustomers; i++) {
            if (!(capacityArray[nCusAndPro + 2][i] == currentArray[nCusAndPro + 2][i])) {
                isFeasible = false;
                break;
            }
        }
        if (!(capacityArray[nCusAndPro + 2][nCusAndPro + 1] == currentArray[nCusAndPro + 2][nCusAndPro + 1])) {
            isFeasible = false;
        }

        if (!isFeasible) {
            BufferedWriter w = new BufferedWriter(new FileWriter(args[1]));
            w.append("-1");
            w.close(); //close a file for writing, don't forget
            System.exit(0);
        }

        for (int i = 0; i < currentArray.length; i++) {
            if (i >= nCusAndPro + 2) {
                for (int j = 0; j < currentArray.length; j++) {
                    capacityArray[i][j] = 0;
                    currentArray[i][j] = 0;
                }
            }
            for (int j = nCusAndPro + 2; j < currentArray.length; j++) {
                capacityArray[i][j] = 0;
                currentArray[i][j] = 0;
            }
        }
        //2. ford fulkerson
        fordFulkersonAlgorithm(0, nCusAndPro + 1, nCusAndPro + 4, nCustomers);

        // konecny vypis
        BufferedWriter w = new BufferedWriter(new FileWriter(args[1]));
        boolean isEmpty = true;
        for (int i = 1; i < nCustomers + 1; i++) {
            for (int j = nCustomers + 1; j < nCusAndPro + 1; j++) {
                if (currentArray[i][j] > 0) {
                    w.append((j - nCustomers) + " ");
                    isEmpty = false;
                }
            }
            if (!isEmpty) {
                w.newLine();
                isEmpty = true;
            }
        }
        w.close();
    }

    static void fordFulkersonAlgorithm(int start, int target, int x, int nCustomers) {
        while (true) {
            LinkedList<Integer> queue = new LinkedList<>();
            int[] tmpArray = new int[x];
            Arrays.fill(tmpArray, -1);
            tmpArray[start] = -2;
            queue.add(start);
            while (tmpArray[target] == -1 && !queue.isEmpty()) {
                int tmp = queue.poll();
                for (int y = 0; y < x; y++) {
                    if (tmpArray[y] == -1) {
                        if (currentArray[y][tmp] > 0 || currentArray[tmp][y] < capacityArray[tmp][y]) {
                            tmpArray[y] = tmp;
                            queue.add(y);
                        }
                    }
                }
            }
            if (tmpArray[target] == -1) {
                break;
            }
            int tmpValue = Integer.MAX_VALUE;
            for (int v = target, u = tmpArray[v]; u >= 0; v = u, u = tmpArray[v]) {
                if (0 < currentArray[v][u]) {//back edge
                    tmpValue = min(tmpValue, currentArray[v][u]);
                } else {//forward edge      
                    tmpValue = min(tmpValue, capacityArray[u][v] - currentArray[u][v]);
                }
            }
            for (int v = target, u = tmpArray[v]; u >= 0; v = u, u = tmpArray[v]) {
                if (currentArray[v][u] <= 0) {
                    currentArray[u][v] += tmpValue;
                } else {
                    currentArray[v][u] -= tmpValue;
                }
            }
        }
    }

}
