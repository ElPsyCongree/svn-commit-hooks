public int getMin(int[] ints) {
    int min = Integer.MAX_VALUE;
    for (int num : ints) {
        if (num < min) {
            min = num;
        }
    } /* todo TestSetup.pause(); other todo */
    return min;
}