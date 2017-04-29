import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

public class MaxFeeTxHandler {
    private UTXOPool pool;
    private double maxFee;
    private ArrayList<Transaction> maxFeeTransactions;
    private HashMap<Transaction, HashSet<Transaction>> graph;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public MaxFeeTxHandler(UTXOPool utxoPool) {
        this.pool = new UTXOPool(utxoPool);
        this.maxFee = 0;
        this.maxFeeTransactions = new ArrayList<Transaction>();
        this.graph = new HashMap<Transaction, HashSet<Transaction>>();
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise. (the difference can be thought of as transaction fee)
     */
    public boolean isValidTx(Transaction tx) {

        // Step 1, 2, 3
        double sumInput = 0;
        Set<UTXO> utxoSet = new HashSet<UTXO>();
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);
            UTXO tmp = new UTXO(input.prevTxHash, input.outputIndex);

            if (utxoSet.contains(tmp))
                return false;

            utxoSet.add(tmp);
            if (pool.contains(tmp)) {
                Transaction.Output output = pool.getTxOutput(tmp);

                if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), input.signature)) {
                    return false;
                }
                sumInput += output.value;
            } else {
                return false;
            }
        }

        // Step 4
        double sumOutput = 0;
        for (int i = 0; i < tx.numOutputs(); i++) {
            Transaction.Output output = tx.getOutput(i);
            if (output.value >= 0) {
                sumOutput += output.value;
            } else {
                return false;
            }
        }

        // Step 5
        return sumInput >= sumOutput;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a set of transactions with maximum total transaction fees -- i.e. maximize the sum over all
     * transactions in the set of (sum of input values - sum of output values)).
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        ArrayList<Transaction> possibleTransactions = new ArrayList<Transaction>();
        for (int i = 0; i < possibleTxs.length; i++) {
            possibleTransactions.add(possibleTxs[i]);
            for (int j = 0; j < possibleTxs.length; j++) {
                if (i != j) {
                    if hasConnection(possibleTxs[i], possibleTxs[j]) {
                        //
                    }
                }
            }
        }

        ArrayList<Transaction> validTransactions = new ArrayList<Transaction>();

        // TODO:
        // 1. Separate transactions into groups which have no connection to other groups
        // 2. When choosing transactions within a group, never choose tx without connection

        handleTxs(possibleTransactions, validTransactions, 0);

        Transaction[] results = new Transaction[maxFeeTransactions.size()];
        results = maxFeeTransactions.toArray(results);
        return results;
    }

    public void handleTxs(ArrayList<Transaction> possibleTxs, ArrayList<Transaction> currentValidTxs, double currentFee) {
        // for tx in transactions
        //   check if tx is valid
        //   if yes: handleTxs(tx, transaction - { tx })
        //   if no: skip to next one
        if (currentFee > this.maxFee) {
            this.maxFee = currentFee;
            this.maxFeeTransactions = currentValidTxs;
        }

        if (possibleTxs.size() == 0)
            return;

        for (Transaction tx: possibleTxs) {
            if (isValidTx(tx)) {
                ArrayList<Transaction> newPossibleTxs = new ArrayList<Transaction>();
                for (Transaction t: possibleTxs) {
                    newPossibleTxs.add(t);
                }
                newPossibleTxs.remove(tx);

                ArrayList<Transaction> newCurrentValidTxs = new ArrayList<Transaction>();
                for (Transaction t: currentValidTxs) {
                    newCurrentValidTxs.add(t);
                }
                newCurrentValidTxs.add(tx);

                double newFee = currentFee + calculateTransactionFee(tx);
                handleTxs(newPossibleTxs, newCurrentValidTxs, newFee);
            }
        }
    }

    public double calculateTransactionFee(ArrayList<Transaction> transactions) {
        double totalTransactionFee = 0;
        for (Transaction tx: transactions) {
            totalTransactionFee += calculateTransactionFee(tx);
        }

        return totalTransactionFee;
    }

    public double calculateTransactionFee(Transaction tx) {
        if (tx == null)
            return 0;

        double sumInput = 0;
        for (Transaction.Input input: tx.getInputs()) {
            Transaction.Output output = pool.getTxOutput(new UTXO(input.prevTxHash, input.outputIndex));
            sumInput += output.value;
        }

        double sumOutput = 0;
        for (int i = 0; i < tx.numOutputs(); i++) {
            Transaction.Output output = tx.getOutput(i);
            sumOutput += output.value;
        }

        return sumInput - sumOutput;
    }

    private boolean hasConnection(Transction a, Transaction b) {

    }
}
