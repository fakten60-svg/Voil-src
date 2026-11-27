package lol.lwes.voil.util.prediction;

public interface KnockbackPredictor {
    KnockbackResult simulate(KnockbackRequest knockbackrequest);

    KnockbackResult simulateFast(KnockbackRequest knockbackrequest);
}
