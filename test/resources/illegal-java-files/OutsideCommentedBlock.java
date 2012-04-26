public int getMin(int[] ints) {
    int min = Integer.MAX_VALUE;
    for (int num : ints) {
        if (num < min) {
            min = num;
        }
    } /* todo  other todo */ TestSetup.pause();
    return min;
}